package su.tagir.apps.radiot.service

import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.*
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Util
import leakcanary.AppWatcher
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.image.ImageConfig
import su.tagir.apps.radiot.image.ImageLoader
import su.tagir.apps.radiot.image.Target
import su.tagir.apps.radiot.model.EntryContentProvider
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.EntryState
import su.tagir.apps.radiot.ui.notification.createMediaNotification
import timber.log.Timber
import java.lang.ref.WeakReference

const val ACTION_PLAY = "action_play"
const val ACTION_RESUME = "action_resume"
const val ACTION_PAUSE = "action_pause"
const val ACTION_STOP = "action_stop"
const val MSG_REGISTER_CLIENT = 1
const val MSG_UNREGISTER_CLIENT = 2
const val MSG_SEEK_TO = 3
const val MSG_REQUEST_PROGRESS = 4
const val MSG_PROGRESS = 5
const val MSG_ACTIVITY_STARTED = 6
const val MSG_ACTIVITY_STOPPED = 7
const val MSG_STATE_CHANGED = 8
const val MSG_ERROR = 9

const val KEY_ERROR = "key_error"
const val KEY_URL = "key_url"
const val KEY_FILE = "key_file"
const val KEY_PROGRESS = "key_progress"

class AudioService : Service(), AudioManager.OnAudioFocusChangeListener {

    companion object {

        fun play(filePath: String?, url: String?, progress: Long, context: Context) {
            val i = newIntent(context)
            i.action = ACTION_PLAY
            i.putExtra(KEY_FILE, filePath)
            i.putExtra(KEY_URL, url)
            i.putExtra(KEY_PROGRESS, progress)
            context.startService(i)
        }

        fun pause(context: Context) {
            val i = newIntent(context)
            i.action = ACTION_PAUSE
            context.startService(i)
        }

        fun resume(context: Context) {
            val i = newIntent(context)
            i.action = ACTION_RESUME
            context.startService(i)
        }

        private fun newIntent(context: Context) = Intent(context, AudioService::class.java)
    }

    internal class IncomingHandler(audioService: AudioService) : Handler() {

        private val serviceRef = WeakReference<AudioService>(audioService)

        override fun handleMessage(msg: Message) {
            val audioService = serviceRef.get() ?: return
            when (msg.what) {
                MSG_REGISTER_CLIENT -> {
                    audioService.client = msg.replyTo
                }
                MSG_UNREGISTER_CLIENT -> {
                    audioService.client = null
                }
                MSG_SEEK_TO ->{
                    audioService.resumePlay()
                    audioService.player?.seekTo(msg.arg1.toLong() * 1000)
                }
                MSG_REQUEST_PROGRESS ->{
                    val duration = audioService.player?.duration?.div(1000) ?: 0L
                    val progress = audioService.player?.currentPosition?.div(1000) ?: 0L
                    audioService.client?.send(Message.obtain(null, MSG_PROGRESS, duration.toInt(), progress.toInt()))
                }
                MSG_ACTIVITY_STARTED ->{
                    audioService.stopForeground(true)
                    audioService.isForeground = false
                    audioService.updateState()
                }
                MSG_ACTIVITY_STOPPED -> {
                    val player = audioService.player
                    if (player?.playbackState == Player.STATE_BUFFERING || player?.playbackState == Player.STATE_READY) {
                        if (player.playWhenReady) {
                            val entry = audioService.getCurrentEntry()
                            val notif = createMediaNotification(entry, false, context = audioService)
                            audioService.startForeground(42, notif)
                            audioService.loadImage(entry)
                            audioService.isForeground = true
                        }
                    } else {
                        audioService.stopSelf()
                    }
                }

                else -> super.handleMessage(msg)
            }
        }
    }

    private lateinit var messenger: Messenger
    private var client: Messenger? = null

    private var player: SimpleExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null

    private val becomingNoisyReceiver = BecomingNoisyReceiver()
    private lateinit var audioManager: AudioManager
    private lateinit var audioFocusRequest: AudioFocusRequest

    private var playOnFocusGain = false
    private var playbackDelayed = false
    private val focusLock = Any()

    private var notificationIcon: Bitmap? = null

    private var isForeground = false
        set(value) {
            field = value
            if (!value) {
                notificationIcon = null
            }
        }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(becomingNoisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))

        val adaptiveTrackSelectionFactory = AdaptiveTrackSelection.Factory()
        trackSelector = DefaultTrackSelector(adaptiveTrackSelectionFactory)

        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        player?.addListener(EventsListener())

        configureAudioManager()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(this)
        }
        unregisterReceiver(becomingNoisyReceiver)
        releasePlayer()
        AppWatcher.objectWatcher.watch(this) //leakcanary
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val url = intent.getStringExtra(KEY_URL)
                val file = intent.getStringExtra(KEY_FILE)
                val progress = intent.getLongExtra(KEY_PROGRESS, 0L)
                playAudioStream(file, url, progress)
            }
            ACTION_RESUME -> resumePlay()
            ACTION_PAUSE -> player?.playWhenReady = false
            ACTION_STOP -> {
                player?.stop()
                isForeground = false
                stopForeground(true)
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent): IBinder? {
        messenger = Messenger(IncomingHandler(this))
        return messenger.binder
    }

    override fun onAudioFocusChange(p0: Int) {
        when (p0) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                synchronized(focusLock) {
                    playOnFocusGain = false
                    playbackDelayed = false
                    player?.playWhenReady = false
                }
            }

            AudioManager.AUDIOFOCUS_GAIN -> {
                if (playbackDelayed || playOnFocusGain) {
                    synchronized(focusLock) {
                        playbackDelayed = false
                        playOnFocusGain = false
                        player?.playWhenReady = true
                    }
                }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                synchronized(focusLock) {
                    playOnFocusGain = player?.playWhenReady == true
                    playbackDelayed = false
                    player?.playWhenReady = false
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                Timber.d("focus loss transient can duck")
            }
        }
    }

    private fun configureAudioManager() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val res: Int

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()

            audioFocusRequest = AudioFocusRequest
                    .Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(this)
                    .setWillPauseWhenDucked(true)
                    .setAudioAttributes(playbackAttributes)
                    .build()

            res = audioManager.requestAudioFocus(audioFocusRequest)
        } else {
            @Suppress("DEPRECATION")
            res = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }

        synchronized(focusLock) {
            when (res) {
                AudioManager.AUDIOFOCUS_REQUEST_FAILED -> {
                }
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> player?.playWhenReady = true
                AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> playbackDelayed = true
            }
        }
    }

    private fun resumePlay() {
        val isPlayerReady = player?.playbackState == Player.STATE_READY
        if (isPlayerReady) {
            player?.playWhenReady = true
        } else {
            val entry = getCurrentEntry()
            if (entry != null) {
                playAudioStream(entry.file, entry.audioUrl, entry.progress)
            }
        }
    }

    private fun playAudioStream(filePath: String?, url: String?, progress: Long) {
        if (filePath == null && url == null) {
            broadcastError("Отсутствует url аудиопотока.")
            return
        }
        val dataSource = if (filePath == null) {
            createHttpMediaSource(url)
        } else {
            val spec = DataSpec(Uri.parse(filePath))
            val fileSource = FileDataSource()
            try {
                fileSource.open(spec)
                val factory = DataSource.Factory { fileSource }
                ProgressiveMediaSource.Factory(factory).createMediaSource(fileSource.uri)
            } catch (e: FileDataSource.FileDataSourceException) {
                Timber.e(e)
                createHttpMediaSource(url)
            }
        }

//        PodcastStateService.saveCurrent(url!!, player?.currentPosition ?: 0, this)
        val cv = ContentValues()
        cv.put("audioUrl", url)
        cv.put("lastProgress", player?.currentPosition ?: 0)
        contentResolver.update(Uri.parse(EntryContentProvider.ENTRY_URI), cv, null, null)

        player?.prepare(dataSource)
        if (progress > 0) {
            player?.seekTo(progress)
        }
        player?.playWhenReady = true
    }

    private fun createHttpMediaSource(url: String?): MediaSource {
        val radioUri = Uri.parse(url)
        val userAgent = Util.getUserAgent(this, "RadioT")
        val mediaDataSourceFactory = DefaultHttpDataSourceFactory(userAgent, DefaultBandwidthMeter.Builder(this).build())
        return ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(radioUri)
    }

    private fun updateState() {
        val loading = player?.playbackState == Player.STATE_BUFFERING

        val isPlaying = (true == player?.isLoading || player?.playbackState == Player.STATE_READY || player?.playbackState == Player.STATE_BUFFERING)
        val state: Int

        when {
            isPlaying -> {
                state = if (player?.playWhenReady == true) EntryState.PLAYING else EntryState.PAUSED
                playOnFocusGain = player?.playWhenReady ?: false
                if (player?.playWhenReady != true) {
                    playbackDelayed = false
                }
            }
            else -> {
                state = EntryState.PAUSED
                playOnFocusGain = false
                playbackDelayed = false
            }
        }
        val progress = player?.currentPosition ?: 0

        val cv = ContentValues()
        cv.put("state", state)
        cv.put("progress", progress)
        contentResolver.update(Uri.parse(EntryContentProvider.UPDATE_ENTRY_URI), cv, null, null)
        Timber.d("state: $state")
        client?.let {
            val message = Message.obtain(null, MSG_STATE_CHANGED, if(loading) 1 else 0, -1)
            client?.send(message)
        }

        if (isForeground) {
            updateNotification()
        }
    }

    private fun releasePlayer() {
        player?.release()
        player = null
        trackSelector = null
    }

    private fun getCurrentEntry(): Entry? {
        var entry: Entry? = null
        val cursor = contentResolver.query(Uri.parse(EntryContentProvider.ENTRY_URI), null, null, null, null)
        cursor?.use { c ->
            if (c.moveToFirst()) {
                entry = Entry(cursor)
            }
        }
        return entry
    }

    private fun updateNotification() {
        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val entry = getCurrentEntry()
        val notif = createMediaNotification(entry, player?.playWhenReady != true, notificationIcon, this)
        if (notificationIcon == null) {
            loadImage(entry)
        }

        notifManager.notify(42, notif)
    }


    private fun broadcastError(error: String?) {
        client?.let {
            val data = Bundle()
            data.putString(KEY_ERROR, error)
            val message = Message.obtain(null, MSG_ERROR, data)
            client?.send(message)
        }
    }

    private fun loadImage(entry: Entry?) {
        entry?.image?.let { url ->

            val target = object : Target {

                override fun onLoaded(bitmap: Bitmap) {
                    notificationIcon = bitmap
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val paused = player?.playWhenReady != true
                    val notification = createMediaNotification(entry, paused, bitmap, this@AudioService)
                    notificationManager.notify(42, notification)
                }
            }

            val config = ImageConfig(context = this)

            ImageLoader.load(url, target, config)
        }
    }

    private inner class EventsListener : Player.EventListener {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            updateState()
        }

        override fun onPlayerError(error: ExoPlaybackException?) {
            val errorStr: String? = when (error?.type) {
                ExoPlaybackException.TYPE_SOURCE -> getString(R.string.error_source)
                else -> getString(R.string.error_occurred)
            }

            broadcastError(errorStr)
        }
    }

    private inner class BecomingNoisyReceiver : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == p1?.action) {
                player?.playWhenReady = false
            }
        }
    }

}
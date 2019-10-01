package su.tagir.apps.radiot.service

import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.RemoteCallbackList
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
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.EntryContentProvider
import su.tagir.apps.radiot.model.PodcastStateService
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.EntryState
import su.tagir.apps.radiot.model.entries.Progress
import su.tagir.apps.radiot.ui.notification.createMediaNotification
import timber.log.Timber


class AudioService : Service(), AudioManager.OnAudioFocusChangeListener {

    companion object {

        const val ACTION_PLAY = "action_play"
        const val ACTION_RESUME = "action_resume"
        const val ACTION_PAUSE = "action_pause"
        const val ACTION_STOP = "action_stop"

        const val KEY_URL = "key_url"
        const val KEY_FILE = "key_file"
        const val KEY_PROGRESS = "key_progress"

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

        fun stop(context: Context) {
            val i = newIntent(context)
            i.action = ACTION_STOP
            context.startService(i)
        }

        private fun newIntent(context: Context) = Intent(context, AudioService::class.java)
    }

    private var player: SimpleExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null
    private val callbackList = RemoteCallbackList<IAudioServiceCallback>()

    private val handler = Handler()

    private val becomingNoisyReceiver = BecomingNoisyReceiver()
    private lateinit var audioManager: AudioManager
    private lateinit var audioFocusRequest: AudioFocusRequest

    private var playOnFocusGain = false
    private var playbackDelayed = false
    private val focusLock = Any()

    private var isForeground = false

    private val service = object : IAudioService.Stub() {
        override fun seekTo(secs: Long) {
            handler.post {
                resumePlay()
                player?.seekTo(secs * 1000)
            }
        }

        override fun getProgress(state: Progress) {
            state.duration = player?.duration?.div(1000) ?: 0L
            state.progress = player?.currentPosition?.div(1000) ?: 0L
        }

        override fun onActivityStarted() {
            stopForeground(true)
            isForeground = false
            updateState()
        }

        override fun onActivityStopped() {
            if (player?.playbackState == Player.STATE_BUFFERING || player?.playbackState == Player.STATE_READY) {
                if (player?.playWhenReady == true) {
                    val notif = createMediaNotification(getCurrentEntry(), false, this@AudioService)
                    startForeground(42, notif)
                    isForeground = true
                }
            } else {
                stopSelf()
            }
        }

        override fun registerCallback(callback: IAudioServiceCallback?) {
            callbackList.register(callback)
        }

        override fun unregisterCallback(callback: IAudioServiceCallback?) {
            callbackList.unregister(callback)
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
        handler.removeCallbacksAndMessages(null)
        releasePlayer()
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
        return service.asBinder()
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
                    .setOnAudioFocusChangeListener(this, handler)
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

        PodcastStateService.saveCurrent(url!!, player?.currentPosition ?: 0, this)

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
        PodcastStateService.updateCurrentPodcastStateAndProgress(state, progress, this)
        val n = callbackList.beginBroadcast()
        for (i in 0 until n) {
            callbackList.getBroadcastItem(i).onStateChanged(loading, state)
        }
        callbackList.finishBroadcast()
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
//                entry = Entry(cursor)
            }
        }
        return entry
    }

    private fun updateNotification() {
        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notif = createMediaNotification(getCurrentEntry(), player?.playWhenReady != true, this)

        notifManager.notify(42, notif)
    }


    private fun broadcastError(error: String?) {
        val n = callbackList.beginBroadcast()
        for (i in 0 until n) {
            callbackList.getBroadcastItem(i).onError(error)
        }
        callbackList.finishBroadcast()
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
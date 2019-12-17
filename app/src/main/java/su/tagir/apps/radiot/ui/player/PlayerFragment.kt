package su.tagir.apps.radiot.ui.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.STREAM_URL
import su.tagir.apps.radiot.di.AppComponent
import su.tagir.apps.radiot.image.ImageConfig
import su.tagir.apps.radiot.image.ImageLoader
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.EntryState
import su.tagir.apps.radiot.model.entries.TimeLabel
import su.tagir.apps.radiot.service.*
import su.tagir.apps.radiot.ui.mvp.BaseMvpFragment
import su.tagir.apps.radiot.utils.convertSeconds
import su.tagir.apps.radiot.utils.visibleGone
import su.tagir.apps.radiot.utils.visibleInvisible
import timber.log.Timber
import java.lang.ref.WeakReference
import kotlin.math.max

class PlayerFragment : BaseMvpFragment<PlayerContract.View,
        PlayerContract.Presenter>(),
        PlayerContract.View,
        ServiceConnection,
        View.OnClickListener,
        TimeLabelsAdapter.Callback {

    private lateinit var btnPlay: View
    private lateinit var btnPause: View
    private lateinit var btnPlayBig: View
    private lateinit var btnPauseBig: View
    private lateinit var image: ImageView
    private lateinit var title: TextView
    private lateinit var progress: ProgressBar
    private lateinit var progressHorizontal: ProgressBar
    private lateinit var seekBar: SeekBar
    private lateinit var progressTime: TextView
    private lateinit var leftTime: TextView
    private lateinit var timeLabels: RecyclerView
    private lateinit var btnForward: ImageButton
    private lateinit var btnReplay: ImageButton
    private lateinit var btnChat: ImageButton
    private lateinit var btnWeb: ImageButton
    private lateinit var logo: ImageView

    private lateinit var timeLabelsAdapter: TimeLabelsAdapter

    private var seeking = false

    private lateinit var messenger: Messenger
    private var service: Messenger? = null

    internal class IncomingHandler(playerFragment: PlayerFragment) : Handler() {

        private val playerFragmentRef = WeakReference<PlayerFragment>(playerFragment)

        override fun handleMessage(msg: Message) {
            val playerFragment = playerFragmentRef.get() ?: return
            when (msg.what) {
                MSG_PROGRESS -> {
                    val duration = msg.arg1
                    val progress = msg.arg2
                    playerFragment.showProgress(duration, progress)
                }
                MSG_STATE_CHANGED -> {
                    val loading = msg.arg1 == 1
                    playerFragment.showLoading(loading)
                }
                MSG_ERROR -> {
                    val error = (msg.obj as? Bundle)?.getString(KEY_ERROR)
                    error?.let {
                        playerFragment.showError(error)
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_player, container, false)
        messenger = Messenger(IncomingHandler(this))
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnPlay = view.findViewById(R.id.btn_play)
        btnPause = view.findViewById(R.id.btn_pause)
        btnPlayBig = view.findViewById(R.id.btn_play_big)
        btnPauseBig = view.findViewById(R.id.btn_pause_big)
        image = view.findViewById(R.id.image)
        title = view.findViewById(R.id.title)
        progress = view.findViewById(R.id.progress)
        progressHorizontal = view.findViewById(R.id.progress_horizontal)
        seekBar = view.findViewById(R.id.seek_bar)
        progressTime = view.findViewById(R.id.progress_time)
        leftTime = view.findViewById(R.id.left_time)
        timeLabels = view.findViewById(R.id.time_labels)
        btnForward = view.findViewById(R.id.btn_forward)
        btnReplay = view.findViewById(R.id.btn_replay)
        btnChat = view.findViewById(R.id.btn_chat)
        btnWeb = view.findViewById(R.id.btn_web)
        logo = view.findViewById(R.id.logo)

        btnPause.setOnClickListener(this)
        btnPauseBig.setOnClickListener(this)
        btnPlay.setOnClickListener(this)
        btnPlayBig.setOnClickListener(this)
        btnChat.setOnClickListener(this)
        btnWeb.setOnClickListener(this)
        title.setOnClickListener(this)
        image.setOnClickListener(this)
        btnForward.setOnClickListener(this)
        btnReplay.setOnClickListener(this)

        ImageLoader.display(R.drawable.ic_radiot, logo)

        timeLabelsAdapter = TimeLabelsAdapter(emptyList(), this)
        timeLabels.adapter = timeLabelsAdapter
        timeLabels.layoutManager = LinearLayoutManager(view.context, RecyclerView.VERTICAL, true)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) {
                    return
                }
                progressTime.text = progress.toLong().convertSeconds()
                leftTime.text = (seekBar.max - progress).toLong().convertSeconds()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                seeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                seekTo(seekBar.progress)
                seeking = false
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        bindService(context)
    }

    override fun onResume() {
        super.onResume()
        bindService(context!!)
    }

    override fun onPause() {
        try {
            val message = Message.obtain(null, MSG_ACTIVITY_STOPPED)
            service?.send(message)
        } catch (e: RemoteException) {
            Timber.e(e)
        }
        context?.unbindService(this)
        super.onPause()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        service = null
    }

    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        service = Messenger(binder)
        try {
            var message = Message.obtain(null, MSG_REGISTER_CLIENT)
            message.replyTo = messenger
            service?.send(message)

            message = Message.obtain(null, MSG_ACTIVITY_STARTED)
            service?.send(message)
        } catch (e: RemoteException) {
            Timber.e(e)
        }

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_pause, R.id.btn_pause_big -> presenter.pause()
            R.id.btn_play, R.id.btn_play_big -> presenter.resume()
            R.id.btn_chat -> presenter.showChat()
            R.id.btn_web -> presenter.openWebPage()
            R.id.btn_forward -> seekTo(seekBar.progress + 30)
            R.id.btn_replay -> seekTo(max(0, seekBar.progress - 30))
        }
    }

    override fun onTimeLabelClick(timeLabel: TimeLabel) {
        presenter.seekTo(timeLabel)
    }

    override fun createPresenter(): PlayerContract.Presenter {
        val appComponent: AppComponent = (activity!!.application as App).appComponent
        return PlayerPresenter(appComponent.entryRepository, appComponent.router)
    }

    override fun showCurrentPodcast(entry: Entry) {
//        Timber.d("entry: $entry")
        btnPause.visibleInvisible(entry.state == EntryState.PLAYING)
        btnPlay.visibleInvisible(entry.state == EntryState.PAUSED || entry.state == EntryState.IDLE)
        btnPauseBig.visibleInvisible(entry.state == EntryState.PLAYING)
        btnPlayBig.visibleInvisible(entry.state == EntryState.PAUSED || entry.state == EntryState.IDLE)

        val config = ImageConfig(placeholder = R.drawable.ic_notification_large, error = R.drawable.ic_notification_large)
        ImageLoader.display(entry.image ?: "empty", image, config)

        title.text = entry.title
        seekBar.isEnabled = entry.url != STREAM_URL
        btnForward.visibleInvisible(entry.url != STREAM_URL)
        btnReplay.visibleInvisible(entry.url != STREAM_URL)
        leftTime.visibleInvisible(entry.url != STREAM_URL)
        progressTime.visibleInvisible(entry.url != STREAM_URL)
    }

    override fun showTimeLabels(timeLabels: List<TimeLabel>) {
        timeLabelsAdapter.update(timeLabels)
    }

    override fun requestProgress() {
        try {
            val message = Message.obtain(null, MSG_REQUEST_PROGRESS)
            service?.send(message)
        } catch (e: RemoteException) {
            Timber.e(e)
        }
    }

    override fun seekTo(seek: Int) {
        try {
            val message = Message.obtain(null, MSG_SEEK_TO, seek, -1)
            service?.send(message)
        } catch (e: RemoteException) {
            Timber.e(e)
        }
    }

    override fun showLoading(loading: Boolean) {
        btnForward.isEnabled = !loading
        btnReplay.isEnabled = !loading
        progress.visibleGone(loading)
        progressHorizontal.visibleInvisible(loading)
        seekBar.visibleInvisible(!loading)
        leftTime.visibleInvisible(!loading)
        progressTime.visibleInvisible(!loading)
    }

    override fun showError(error: String) {
        AlertDialog.Builder(context!!)
                .setTitle(R.string.error)
                .setMessage(error)
                .setPositiveButton("OK", null)
                .create()
                .show()
    }

    private fun bindService(context: Context?) {
        context?.bindService(Intent(context,
                AudioService::class.java), this, Context.BIND_AUTO_CREATE)
    }


    override fun onSlide(offset: Float) {
        val alpha = 1 - 5 * offset
        btnPause.alpha = alpha
        btnPlay.alpha = alpha
        btnPlay.isEnabled = btnPlay.alpha > 0
        btnPause.isEnabled = btnPause.alpha > 0
        progress.alpha = alpha
        btnChat.alpha = -alpha
        btnWeb.alpha = -alpha
        btnChat.visibleInvisible(btnChat.alpha > 0)
        btnWeb.visibleInvisible(btnWeb.alpha > 0)
    }

    private fun showProgress(duration: Int, progress: Int) {
        if (seeking) {
            return
        }
        if (seekBar.max != duration) {
            seekBar.max = duration
        }
        seekBar.progress = progress
        progressTime.text = progress.convertSeconds()

        val leftTime = duration - progress
        this.leftTime.text = (if (leftTime >= 0) leftTime else 0).convertSeconds()
    }

}
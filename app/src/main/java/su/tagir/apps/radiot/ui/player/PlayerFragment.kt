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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.GlideApp
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.STREAM_URL
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.EntryState
import su.tagir.apps.radiot.model.entries.Progress
import su.tagir.apps.radiot.model.entries.TimeLabel
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.service.AudioService
import su.tagir.apps.radiot.service.IAudioService
import su.tagir.apps.radiot.service.IAudioServiceCallback
import su.tagir.apps.radiot.ui.mvp.BaseMvpFragment
import su.tagir.apps.radiot.utils.convertSeconds
import su.tagir.apps.radiot.utils.visibleGone
import su.tagir.apps.radiot.utils.visibleInvisible
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject
import kotlin.math.max

class PlayerFragment : BaseMvpFragment<PlayerContract.View,
        PlayerContract.Presenter>(),
        PlayerContract.View,
        ServiceConnection,
        Injectable,
        View.OnClickListener,
        TimeLabelsAdapter.Callback {

    @Inject
    lateinit var entryRepository: EntryRepository

    @Inject
    lateinit var router: Router

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

    private var audioService: IAudioService? = null

    private val requestHandler = RequestHandler(this)

    private val serviceCallback = object : IAudioServiceCallback.Stub(){
        override fun onStateChanged(loading: Boolean, state: Int) {
            val msg = requestHandler.obtainMessage(0, -1, -1, loading)
            requestHandler.sendMessage(msg)
        }

        override fun onError(error: String?) {
            val msg = requestHandler.obtainMessage(1, -1, -1, error)
            requestHandler.sendMessage(msg)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_player, container, false)

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

        GlideApp.with(this)
                .load(R.drawable.ic_radiot)
                .into(logo)

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
                seekTo(seekBar.progress.toLong())
                seeking = false
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        bindService(context)
    }

    override fun onDetach() {
        try {
            audioService?.unregisterCallback(serviceCallback)
            audioService?.onActivityStopped()
        } catch (e: RemoteException) {
            Timber.e(e)
        }
        context!!.unbindService(this)
        super.onDetach()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        try {
            audioService?.unregisterCallback(serviceCallback)
        } catch (e: RemoteException) {
            Timber.e(e)
        }
        audioService = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        audioService = IAudioService.Stub.asInterface(service)
        try {
            audioService?.registerCallback(serviceCallback)
            audioService?.onActivityStarted()
        } catch (e: RemoteException) {
            Timber.e(e)
        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.btn_pause, R.id.btn_pause_big -> presenter.pause()
            R.id.btn_play, R.id.btn_play_big -> presenter.resume()
            R.id.chat -> presenter.showChat()
            R.id.btn_web -> presenter.openWebPage()
            R.id.btn_forward -> seekTo(seekBar.progress.toLong() + 30L)
            R.id.btn_replay -> seekTo(max(0L, seekBar.progress.toLong() - 30L))
        }
    }

    override fun onTimeLabelClick(timeLabel: TimeLabel) {
        presenter.seekTo(timeLabel)
    }

    override fun createPresenter(): PlayerContract.Presenter =
            PlayerPresenter(entryRepository, router)

    override fun showCurrentPodcast(entry: Entry) {
        btnPause.visibleInvisible(entry.state == EntryState.PLAYING)
        btnPlay.visibleInvisible(entry.state == EntryState.PAUSED || entry.state == EntryState.IDLE)
        btnPauseBig.visibleInvisible(entry.state == EntryState.PLAYING)
        btnPlayBig.visibleInvisible(entry.state == EntryState.PAUSED || entry.state == EntryState.IDLE)

        GlideApp.with(this@PlayerFragment)
                .load(entry.image)
                .placeholder(R.drawable.ic_notification_large)
                .error(R.drawable.ic_notification_large)
                .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.item_image_corner_radius)))
                .into(image)

        title.text = entry.title
        seekBar.isEnabled = entry.url != STREAM_URL
        btnForward.visibleInvisible(entry.url != STREAM_URL)
        btnReplay.visibleInvisible(entry.url != STREAM_URL)
        leftTime.visibleInvisible(entry.url != STREAM_URL)
        progressTime.visibleInvisible(entry.url != STREAM_URL)
    }

    override fun showTimeLabels(timeLabels: List<TimeLabel>) {
        Timber.d("showTimeLabels: $timeLabels")
        timeLabelsAdapter.update(timeLabels)
    }

    override fun requestProgress() {
        val progress = Progress()
        audioService?.getProgress(progress)
        showProgress(progress)
    }

    override fun seekTo(seek: Long) {
        try {
            audioService?.seekTo(seek)
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

    private fun bindService(context: Context) {
        val intent = Intent(context, AudioService::class.java)
        intent.action = IAudioService::class.java.name
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
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

    private fun showProgress(progress: Progress?) {
        if (seeking) {
            return
        }
        val duration = progress?.duration?.toInt() ?: 0
        if (seekBar.max != duration) {
            seekBar.max = duration
        }
        seekBar.progress = progress?.progress?.toInt() ?: 0
        progressTime.text = progress?.progress?.convertSeconds()

        val leftTime = progress?.duration?.minus(progress.progress) ?: 0
        this.leftTime.text = (if (leftTime >= 0) leftTime else 0).convertSeconds()
    }


    class RequestHandler(playerFragment: PlayerFragment): Handler() {
        private val weakRef = WeakReference(playerFragment)

        override fun handleMessage(msg: Message) {
            when(msg.what){
                0 ->  weakRef.get()?.showLoading(msg.obj as Boolean)
                1 -> {
                    msg.obj?.let {error ->
                        weakRef.get()?.showError(error as String)
                    }
                }
            }
        }
    }

}
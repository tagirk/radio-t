package su.tagir.apps.radiot.ui.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindDimen
import butterknife.BindView
import butterknife.OnClick
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import io.reactivex.Observable
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
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
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

class PlayerFragment: BaseMvpFragment<PlayerContract.View,
        PlayerContract.Presenter>(),
        PlayerContract.View,
        ServiceConnection,
        Injectable {

    @Inject
    lateinit var entryRepository: EntryRepository

    @Inject
    lateinit var scheduler: BaseSchedulerProvider

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @BindView(R.id.btn_play)
    lateinit var btnPlay: View

    @BindView(R.id.btn_pause)
    lateinit var btnPause: View

    @BindView(R.id.btn_play_big)
    lateinit var btnPlayBig: View

    @BindView(R.id.btn_pause_big)
    lateinit var btnPauseBig: View

    @BindView(R.id.image)
    lateinit var image: ImageView

    @BindView(R.id.title)
    lateinit var title: TextView

    @BindView(R.id.progress)
    lateinit var progress: ProgressBar

    @BindView(R.id.progress_horizontal)
    lateinit var progressHorizontal: ProgressBar

    @BindView(R.id.seek_bar)
    lateinit var seekBar: SeekBar

    @BindView(R.id.progress_time)
    lateinit var progressTime: TextView

    @BindView(R.id.left_time)
    lateinit var leftTime: TextView

    @BindView(R.id.time_labels)
    lateinit var timeLabels: RecyclerView

    @BindView(R.id.btn_forward)
    lateinit var btnForward: ImageButton

    @BindView(R.id.btn_replay)
    lateinit var btnReplay: ImageButton

    @BindView(R.id.btn_chat)
    lateinit var btnChat: ImageButton

    @BindView(R.id.btn_web)
    lateinit var btnWeb: ImageButton

    @BindView(R.id.logo)
    lateinit var logo: ImageView

    @JvmField
    @BindDimen(R.dimen.item_image_corner_radius)
    var cornerRadius: Int = 0

    @JvmField
    @BindDimen(R.dimen.player_image_size)
    var imageSize: Int = 0

    @JvmField
    @BindDimen(R.dimen.load_progress_padding)
    var progressPad: Int = 0

    private lateinit var timeLabelsAdapter: TimeLabelsAdapter

    private var seeking = false

    private var audioService: IAudioService? = null

    private val serviceCallback = AudioServiceCallback(this)

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View =
            inflater.inflate(R.layout.fragment_player, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlideApp.with(this)
                .load(R.drawable.ic_radiot)
                .into(logo)

        timeLabelsAdapter = TimeLabelsAdapter(emptyList())
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

    override fun onResume() {
        super.onResume()
        presenter.setListener(activity as PlayerContract.Presenter.InteractionListener)
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

    @OnClick(R.id.btn_pause, R.id.btn_pause_big)
    fun pause() {
       presenter.pause()
    }

    @OnClick(R.id.btn_play, R.id.btn_play_big)
    fun resume() {
        presenter.resume()
    }

    @OnClick(R.id.btn_chat)
    fun chat() {
        presenter.onChatClick()
    }

    @OnClick(R.id.btn_web)
    fun openWebPage() {
        presenter.openWebPage()
    }

    @OnClick(R.id.title, R.id.image)
    fun expand() {
        presenter.onTitleClick()
    }

    @OnClick(R.id.btn_forward)
    fun forward() {
        seekTo(seekBar.progress.toLong() + 30L)
    }

    @OnClick(R.id.btn_replay)
    fun replay() {
        seekTo(Math.max(0L, seekBar.progress.toLong() - 30L))
    }

    override fun createPresenter(): PlayerContract.Presenter =
            PlayerPresenter(entryRepository, scheduler, router)

    override fun showCurrentPodcast(entry: Entry) {
        btnPause.visibleInvisible(entry.state == EntryState.PLAYING)
        btnPlay.visibleInvisible(entry.state == EntryState.PAUSED)
        btnPauseBig.visibleInvisible(entry.state == EntryState.PLAYING)
        btnPlayBig.visibleInvisible(entry.state == EntryState.PAUSED)

        GlideApp.with(this@PlayerFragment)
                .load(entry.image)
                .placeholder(R.drawable.ic_notification_large)
                .error(R.drawable.ic_notification_large)
                .transform(RoundedCorners(cornerRadius))
                .into(image)

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
        seekBar.visibleInvisible(loading)
        leftTime.visibleInvisible(loading)
        progressTime.visibleInvisible(loading)
    }

    override fun timeLabelRequests(): Observable<TimeLabel> = timeLabelsAdapter.labels()

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

    class AudioServiceCallback(playerFragment: PlayerFragment) : IAudioServiceCallback.Stub() {

        private val weakRef = WeakReference(playerFragment)

        override fun onStateChanged(loading: Boolean, state: Int) {
            weakRef.get()?.showLoading(loading)
        }

        override fun onError(error: String?) {
            error?.let{
                weakRef.get()?.showError(error)
            }
        }
    }

}
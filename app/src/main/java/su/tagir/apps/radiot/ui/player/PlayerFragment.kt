package su.tagir.apps.radiot.ui.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.STREAM_URL
import su.tagir.apps.radiot.databinding.FragmentPlayerBinding
import su.tagir.apps.radiot.di.AppComponent
import su.tagir.apps.radiot.image.ImageConfig
import su.tagir.apps.radiot.image.ImageLoader
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.EntryState
import su.tagir.apps.radiot.model.entries.TimeLabel
import su.tagir.apps.radiot.service.*
import su.tagir.apps.radiot.ui.mvp.MvpFragment
import su.tagir.apps.radiot.utils.convertSeconds
import su.tagir.apps.radiot.utils.visibleGone
import su.tagir.apps.radiot.utils.visibleInvisible
import timber.log.Timber
import java.lang.ref.WeakReference
import kotlin.math.max

class PlayerFragment : MvpFragment<PlayerContract.View,
        PlayerContract.Presenter>(R.layout.fragment_player),
        PlayerContract.View,
        ServiceConnection,
        View.OnClickListener,
        TimeLabelsAdapter.Callback {

    private val binding: FragmentPlayerBinding by viewBinding()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        messenger = Messenger(IncomingHandler(this))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPause.setOnClickListener(this)
        binding.btnPauseBig.setOnClickListener(this)
        binding.btnPlay.setOnClickListener(this)
        binding.btnPlayBig.setOnClickListener(this)
        binding.btnChat.setOnClickListener(this)
        binding.btnWeb.setOnClickListener(this)
        binding.title.setOnClickListener(this)
        binding.image.setOnClickListener(this)
        binding.btnForward.setOnClickListener(this)
        binding.btnReplay.setOnClickListener(this)

        ImageLoader.display(R.drawable.ic_radiot, binding.logo)

        timeLabelsAdapter = TimeLabelsAdapter(emptyList(), this)
        binding.timeLabels.adapter = timeLabelsAdapter
        binding.timeLabels.layoutManager = LinearLayoutManager(view.context, RecyclerView.VERTICAL, true)

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) {
                    return
                }
                binding.progressTime.text = progress.toLong().convertSeconds()
                binding.leftTime.text = (seekBar.max - progress).toLong().convertSeconds()
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


    override fun onResume() {
        super.onResume()
        bindService(requireContext())
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
            R.id.btn_forward -> seekTo(binding.seekBar.progress + 30)
            R.id.btn_replay -> seekTo(max(0, binding.seekBar.progress - 30))
        }
    }

    override fun onTimeLabelClick(timeLabel: TimeLabel) {
        presenter.seekTo(timeLabel)
    }

    override fun createPresenter(): PlayerContract.Presenter {
        val appComponent: AppComponent = (requireActivity().application as App).appComponent
        return PlayerPresenter(appComponent.entryRepository, appComponent.router)
    }

    override fun showCurrentPodcast(entry: Entry) {
//        Timber.d("entry: $entry")
        binding.btnPause.visibleInvisible(entry.state == EntryState.PLAYING)
        binding.btnPlay.visibleInvisible(entry.state == EntryState.PAUSED || entry.state == EntryState.IDLE)
        binding.btnPauseBig.visibleInvisible(entry.state == EntryState.PLAYING)
        binding.btnPlayBig.visibleInvisible(entry.state == EntryState.PAUSED || entry.state == EntryState.IDLE)

        val config = ImageConfig(placeholder = R.drawable.ic_notification_large, error = R.drawable.ic_notification_large)
        ImageLoader.display(entry.image ?: "empty", binding.image, config)

        binding.title.text = entry.title
        binding.seekBar.isEnabled = entry.url != STREAM_URL
        binding.btnForward.visibleInvisible(entry.url != STREAM_URL)
        binding.btnReplay.visibleInvisible(entry.url != STREAM_URL)
        binding.leftTime.visibleInvisible(entry.url != STREAM_URL)
        binding.progressTime.visibleInvisible(entry.url != STREAM_URL)
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
        binding.btnForward.isEnabled = !loading
        binding.btnReplay.isEnabled = !loading
        binding.progress.visibleGone(loading)
        binding.progressHorizontal.visibleInvisible(loading)
        binding.seekBar.visibleInvisible(!loading)
        binding.leftTime.visibleInvisible(!loading)
        binding.progressTime.visibleInvisible(!loading)
    }

    override fun showError(error: String) {
        AlertDialog.Builder(requireContext())
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
        binding.btnPause.alpha = alpha
        binding.btnPlay.alpha = alpha
        binding.btnPlay.isEnabled = binding.btnPlay.alpha > 0
        binding.btnPause.isEnabled = binding.btnPause.alpha > 0
        binding.progress.alpha = alpha
        binding.btnChat.alpha = -alpha
        binding.btnWeb.alpha = -alpha
        binding.btnChat.visibleInvisible(binding.btnChat.alpha > 0)
        binding.btnWeb.visibleInvisible(binding.btnWeb.alpha > 0)
    }

    private fun showProgress(duration: Int, progress: Int) {
        if (seeking) {
            return
        }
        if (binding.seekBar.max != duration) {
            binding.seekBar.max = duration
        }
        binding.seekBar.progress = progress
        binding.progressTime.text = progress.convertSeconds()

        val leftTime = duration - progress
        binding.leftTime.text = (if (leftTime >= 0) leftTime else 0).convertSeconds()
    }

}
package su.tagir.apps.radiot.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindDimen
import butterknife.BindView
import butterknife.OnClick
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import su.tagir.apps.radiot.GlideApp
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.STREAM_URL
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.EntryState
import su.tagir.apps.radiot.model.entries.Progress
import su.tagir.apps.radiot.model.entries.TimeLabel
import su.tagir.apps.radiot.ui.common.BaseFragment
import su.tagir.apps.radiot.utils.convertSeconds
import su.tagir.apps.radiot.utils.visibleGone
import su.tagir.apps.radiot.utils.visibleInvisible
import javax.inject.Inject

class PlayerFragment : BaseFragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var playerViewModel: PlayerViewModel

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

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View =
            inflater.inflate(R.layout.fragment_player, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlideApp.with(this)
                .load(R.drawable.ic_radiot)
                .into(logo)

        timeLabelsAdapter = TimeLabelsAdapter(emptyList(), object : TimeLabelsAdapter.Callback {
            override fun onItemClick(item: TimeLabel) {
                if (item.time != null) {
                    playerViewModel.seekTo(item.time / 1000)
                }
            }
        })
        timeLabels.adapter = timeLabelsAdapter
        timeLabels.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, true)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        playerViewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(PlayerViewModel::class.java)

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
                playerViewModel.seekTo(seekBar.progress.toLong())
                seeking = false
            }
        })

        observePlayerViewModel()
    }

    @OnClick(R.id.btn_pause, R.id.btn_pause_big)
    fun pause() {
        playerViewModel.onPauseClick()
    }

    @OnClick(R.id.btn_play, R.id.btn_play_big)
    fun resume() {
        playerViewModel.onResumeClick()
    }

    @OnClick(R.id.btn_chat)
    fun chat() {
        playerViewModel.onChatClick()
    }

    @OnClick(R.id.btn_web)
    fun openWebPage() {
        playerViewModel.openWebPage()
    }

    @OnClick(R.id.title, R.id.image)
    fun expand() {
        playerViewModel.onExpandClick()
    }

    @OnClick(R.id.btn_forward)
    fun forward() {
        playerViewModel.seekTo(seekBar.progress.toLong() + 30L)
    }

    @OnClick(R.id.btn_replay)
    fun replay() {
        playerViewModel.seekTo(Math.max(0L, seekBar.progress.toLong() - 30L))
    }

    private fun observePlayerViewModel() {
        playerViewModel
                .getCurrentPodcast()
                .observe(getViewLifecycleOwner()!!,
                        Observer { entry ->
                            btnPause.visibleInvisible(entry?.state?.equals(EntryState.PLAYING)
                                    ?: false)
                            btnPlay.visibleInvisible(entry?.state?.equals(EntryState.PAUSED)
                                    ?: false)
                            btnPauseBig.visibleInvisible(entry?.state?.equals(EntryState.PLAYING)
                                    ?: false)
                            btnPlayBig.visibleInvisible(entry?.state?.equals(EntryState.PAUSED)
                                    ?: false)

                            GlideApp.with(this@PlayerFragment)
                                    .load(entry?.image)
                                    .placeholder(R.drawable.ic_notification_large)
                                    .error(R.drawable.ic_notification_large)
                                    .transform(RoundedCorners(cornerRadius))
                                    .into(image)

                            title.text = entry?.title
                            seekBar.isEnabled = entry?.url != STREAM_URL
                            btnForward.visibleInvisible(entry?.url != STREAM_URL)
                            btnReplay.visibleInvisible(entry?.url != STREAM_URL)
                            leftTime.visibleInvisible(entry?.url != STREAM_URL)
                            progressTime.visibleInvisible(entry?.url != STREAM_URL)
                        })

        playerViewModel
                .getError()
                .observe(getViewLifecycleOwner()!!,
                        Observer { error ->
                            if (context != null) {
                                AlertDialog.Builder(context!!)
                                        .setTitle(R.string.error)
                                        .setMessage(error)
                                        .setPositiveButton("OK", null)
                                        .create()
                                        .show()
                            }
                        })

        playerViewModel
                .isLoading()
                .observe(getViewLifecycleOwner()!!,
                        Observer { loading ->
                            btnForward.isEnabled = loading == false
                            btnReplay.isEnabled = loading == false
                            progress.visibleGone(loading ?: false)
                            progressHorizontal.visibleInvisible(loading ?: false)
                            seekBar.visibleInvisible(loading == null || loading == false)
                            leftTime.visibleInvisible(loading == null || loading == false)
                            progressTime.visibleInvisible(loading == null || loading == false)
                        })

        playerViewModel
                .getSlidingValue()
                .observe(getViewLifecycleOwner()!!,
                        Observer { slideOffset -> onSlide(slideOffset ?: 0f) })

        playerViewModel
                .getProgress()
                .observe(getViewLifecycleOwner()!!,
                        Observer { progress -> showProgress(progress) })

        playerViewModel
                .getTimeLabels()
                .observe(getViewLifecycleOwner()!!,
                        Observer { timeLabels ->
                            timeLabelsAdapter.update(timeLabels)
                        })
    }


    private fun onSlide(slideOffset: Float) {
        val alpha = 1 - 5 * slideOffset
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
}
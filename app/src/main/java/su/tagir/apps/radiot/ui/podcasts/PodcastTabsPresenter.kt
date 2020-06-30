package su.tagir.apps.radiot.ui.podcasts

import android.app.Application
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.STREAM_URL
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.ui.mvp.MainDispatcher
import su.tagir.apps.radiot.ui.mvp.MvpBasePresenter
import su.tagir.apps.radiot.utils.startTimer
import java.util.*
import kotlin.math.floor

class PodcastTabsPresenter(private val entryRepository: EntryRepository,
                           dispatcher: CoroutineDispatcher = MainDispatcher(),
                           private val application: Application) : MvpBasePresenter<PodcastsTabsContract.View>(dispatcher), PodcastsTabsContract.Presenter {

    private val nextShow: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))

    private var timerJob: Job? = null

    init {
        nextShow.set(Calendar.HOUR_OF_DAY, 23)
        nextShow.set(Calendar.MINUTE, 0)
        nextShow.set(Calendar.SECOND, 0)
        var dayOfWeek = nextShow.get(Calendar.DAY_OF_WEEK)
        while (dayOfWeek != Calendar.SATURDAY) {
            nextShow.add(Calendar.DAY_OF_WEEK, 1)
            dayOfWeek = nextShow.get(Calendar.DAY_OF_WEEK)
        }
    }

    override fun doOnAttach(view: PodcastsTabsContract.View) {
        observeCurrentPodcast()
    }

    override fun observeCurrentPodcast() {
        launch {
            entryRepository.getCurrent()
                    .collect { entry ->
                        val isStream = STREAM_URL == entry?.audioUrl
                        ifViewAttached({v -> v.showOrHideStream(!isStream)})
                        if (!isStream) {
                            startTimer()
                        } else {
                            timerJob?.cancel()
                        }
                    }
        }
    }

    override fun startTimer() {
        timerJob?.cancel()
        timerJob = launch {
            startTimer(repeatMillis = 1000L) {
                val time = convertTime(nextShow.timeInMillis)
                ifViewAttached({v -> v.showStreamTime(time)})
            }
        }
    }

    override fun playStream() {
        launch {
            entryRepository.playStream(STREAM_URL)
        }
    }

    private fun convertTime(nextShowTime: Long): String {
        val timeMoscow = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))

        val totalSeconds = floor((nextShowTime - timeMoscow.timeInMillis) * 0.001).toInt()

        if (totalSeconds < 0) {
            return "Вещаем!"
        }

        val days = totalSeconds / (24 * 3600)
        var seconds = totalSeconds % (24 * 3600)
        val hours = seconds / 3600
        seconds %= 3600
        val minutes = seconds / 60
        seconds %= 60

        var result = ""
        if (days > 0) {
            result += application.resources.getQuantityString(R.plurals.days, days, days) + " "
        }
        result += application.resources.getString(R.string.formatted_time, hours, minutes, seconds)

        return result
    }
}
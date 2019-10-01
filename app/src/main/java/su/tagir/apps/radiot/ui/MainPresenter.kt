package su.tagir.apps.radiot.ui

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.STREAM_URL
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.ui.mvp.BasePresenter
import su.tagir.apps.radiot.utils.startTimer
import java.util.*
import kotlin.math.floor

class MainPresenter(private val entryRepository: EntryRepository,
                    private val router: Router,
                    dispatcher: CoroutineDispatcher = MainDispatcher()) : BasePresenter<MainContract.View>(dispatcher), MainContract.Presenter {


    private val nextShow: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))

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

    override fun doOnAttach(view: MainContract.View) {
        observeCurrentPodcast()
        startTimerToNextShow()
    }

    override fun observeCurrentPodcast() {
        launch {
            entryRepository.getCurrent()
                    .collect { view?.showCurrentPodcast(it) }
        }
    }

    override fun startTimerToNextShow() {
        launch {
            startTimer(repeatMillis = 1000L) {
                val time = convertTime(nextShow.timeInMillis)
                view?.showTime(time)
            }
        }
    }

    override fun navigateToPodcasts() {
        router.newRootScreen(Screens.PdcastsScreen)
    }

    override fun navigateToNews() {
        router.newRootScreen(Screens.NewsScreen)
    }

    override fun navigateToSettings() {
        router.navigateTo(Screens.SettingsScreen)
    }

    override fun navigateToChat() {
        router.navigateTo(Screens.ChatScreen)
    }

    override fun navigateToPirates() {
        router.navigateTo(Screens.PiratesScreen)
    }

    override fun navigateToAbout() {
        router.navigateTo(Screens.AboutScreen)
    }

    override fun navigateToCredits() {
        router.navigateTo(Screens.CreditsScreen)
    }

    override fun playStream() {
        launch {
            entryRepository.playStream(STREAM_URL)
        }
    }

    override fun pause() {
        entryRepository.pause()
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

        var result = "До эфира\n"
        if (days > 0) {
            result += "$days д. "
        }
        if (hours > 0) {
            result += String.format("%02d", hours) + " ч. "
        }
        if (minutes > 0) {
            result += String.format("%02d", minutes) + " м. "
        }
        result += String.format("%02d ", seconds) + " с."

        return result
    }
}
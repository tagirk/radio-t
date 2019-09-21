package su.tagir.apps.radiot.ui

import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.BasePresenter
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.floor

class MainPresenter(private val router: Router,
                    private val scheduler: BaseSchedulerProvider): BasePresenter<MainContract.View>(), MainContract.Presenter {

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
        startTimerToNextShow()
    }

    override fun startTimerToNextShow() {
        disposables += Observable.interval(0L, 1L, TimeUnit.SECONDS)
                .observeOn(scheduler.ui())
                .subscribe({
                    val time = convertTime(nextShow.timeInMillis)
                    view?.showTime(time)
                }, { Timber.e(it) })

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
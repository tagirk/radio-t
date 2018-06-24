package su.tagir.apps.radiot.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.viewmodel.BaseViewModel
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainViewModel @Inject constructor(private val router: Router,
                                        scheduler: BaseSchedulerProvider) : BaseViewModel(scheduler) {

    private val currentScreen = MutableLiveData<String>()
    private val nextShow: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))
    private val timer = MutableLiveData<String>()

    private var timerDisposable: Disposable? = null

    fun setCurrentScreen(screen: String) {
        this.currentScreen.value = screen
    }

    fun getCurrentScreen(): LiveData<String> = currentScreen

    fun navigateToPodcasts() {
        router.newRootScreen(Screens.PODCASTS_SCREEN)
    }

    fun navigateToStream() {
        router.newRootScreen(Screens.STREAM_SCREEN)
    }

    fun navigateToNews() {
        router.newRootScreen(Screens.NEWS_SCREEN)
    }

    fun navigateToSettings() {
        router.navigateTo(Screens.SETTINGS_SCREEN)
    }

    fun navigateToChat() {
        router.navigateTo(Screens.CHAT_ACTIVITY)
    }

    fun openWebSite(url: String) {
        router.navigateTo(Screens.WEB_SCREEN, url)
    }

    fun navigateToPirates() {
        router.navigateTo(Screens.PIRATES_SCREEN)
    }

    fun navigateToSearch() {
        router.navigateTo(Screens.SEARCH_SCREEN)
    }

    fun back() {
        router.exit()
    }

    fun start() {
        timerDisposable = Observable.just(1)
                .doOnNext { setNextShowTime() }
                .flatMap { Observable.interval(0L, 1L, TimeUnit.SECONDS) }
                .subscribeOn(scheduler.computation())
                .subscribe({ timer.postValue(updateTimer()) }, { Timber.e(it) })

        addDisposable(timerDisposable!!)
    }

    fun stop() {
        timerDisposable?.dispose()
    }

    fun getTimer(): LiveData<String> = timer

    private fun setNextShowTime() {
        nextShow.set(Calendar.HOUR_OF_DAY, 23)
        nextShow.set(Calendar.MINUTE, 0)
        nextShow.set(Calendar.SECOND, 0)
        var dayOfWeek = nextShow.get(Calendar.DAY_OF_WEEK)
        while (dayOfWeek != Calendar.SATURDAY) {
            nextShow.add(Calendar.DAY_OF_WEEK, 1)
            dayOfWeek = nextShow.get(Calendar.DAY_OF_WEEK)
        }
    }

    private fun updateTimer(): String {
        val timeMoscow = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))

        val totalSeconds = Math.floor((nextShow.timeInMillis - timeMoscow.timeInMillis) * 0.001).toInt()

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
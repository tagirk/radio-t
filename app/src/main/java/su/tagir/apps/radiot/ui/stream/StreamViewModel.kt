package su.tagir.apps.radiot.ui.stream

import android.arch.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import su.tagir.apps.radiot.model.entries.Article
import su.tagir.apps.radiot.model.repository.NewsRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.common.SingleLiveEvent
import su.tagir.apps.radiot.ui.viewmodel.ListViewModel
import su.tagir.apps.radiot.ui.viewmodel.State
import su.tagir.apps.radiot.ui.viewmodel.Status
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class StreamViewModel @Inject constructor(
        private val newsRepository: NewsRepository,
        schedulerProvider: BaseSchedulerProvider) : ListViewModel<Article>(schedulerProvider) {

    private var loadDisposable: Disposable? = null
    private val nextShow: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))
    val showTimer = MutableLiveData<Boolean>()
    val timer = MutableLiveData<String?>()
    val error = SingleLiveEvent<String>()

    private var timerDisposable: Disposable? = null
    private lateinit var activeThemeDisposable: Disposable

    init {
        showTimer.value = newsRepository.showTimer
        disposable += newsRepository.getArticles()
                .subscribe({
                    val newState = state.value?.copy(data = it)
                    state.value = newState
                }, { Timber.e(it) })

        loadData()
    }

    override fun loadData() {
        loadDisposable?.dispose()
        loadDisposable = newsRepository.updateArticles()
                .observeOn(scheduler.ui())
                .doOnSubscribe { state.value = if (state.value == null) State(Status.LOADING) else state.value?.copy(Status.LOADING) }
                .subscribe({ state.value = state.value?.copy(status = Status.SUCCESS) },
                        {
                            Timber.e(it)
                            state.value = state.value?.copy(status = Status.ERROR)
                        })

        disposable += loadDisposable!!
    }

    override fun requestUpdates() {
        loadDisposable?.dispose()
        loadDisposable = newsRepository.updateArticles()
                .observeOn(scheduler.ui())
                .subscribe({ state.value = state.value?.copy(status = Status.SUCCESS) },
                        {
                            Timber.e(it)
                            state.value = state.value?.copy(status = Status.ERROR)
                        })

        disposable += loadDisposable!!
    }

    fun showTimer() {
        showTimer.value = true
        newsRepository.showTimer = true
        startTimer()
    }

    fun hideTimer() {
        showTimer.value = false
        newsRepository.showTimer = false
        stopTimer()
    }

    fun dispose() {
        activeThemeDisposable.dispose()
        stopTimer()
    }

    fun startTimer() {
        if (newsRepository.showTimer) {
            timerDisposable = Observable.just(1)
                    .doOnNext { setNextShowTime() }
                    .flatMap { Observable.interval(0L, 1L, TimeUnit.SECONDS) }
                    .subscribeOn(scheduler.computation())
                    .subscribe({ timer.postValue(updateTimer()) }, { Timber.e(it) })

            addDisposable(timerDisposable!!)
        }
    }

    private fun stopTimer() {
        timerDisposable?.dispose()
    }

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

    fun updateActiveTheme() {
        activeThemeDisposable = Observable.interval(1L, 1L, TimeUnit.MINUTES)
                .flatMapSingle { newsRepository.updateActiveArticle().subscribeOn(scheduler.io()) }
                .observeOn(scheduler.ui())
                .retry()
                .subscribe({}, { Timber.e(it) })

        disposable += activeThemeDisposable
    }
}
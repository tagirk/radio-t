package su.tagir.apps.radiot.ui.news

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import su.tagir.apps.radiot.model.entries.Article
import su.tagir.apps.radiot.model.repository.NewsRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.Status
import su.tagir.apps.radiot.ui.mvp.ViewState
import su.tagir.apps.radiot.ui.viewmodel.ListViewModel
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class ArticlesViewModel @Inject constructor(
        private val newsRepository: NewsRepository,
        schedulerProvider: BaseSchedulerProvider) : ListViewModel<Article>(schedulerProvider) {

    private var loadDisposable: Disposable? = null

    private lateinit var activeThemeDisposable: Disposable

    init {
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
                .doOnSubscribe { state.value = if (state.value == null) ViewState(Status.LOADING) else state.value?.copy(Status.LOADING) }
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

    fun dispose() {
        activeThemeDisposable.dispose()
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
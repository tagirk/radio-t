package su.tagir.apps.radiot.ui.news

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Article
import su.tagir.apps.radiot.model.repository.NewsRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.BaseListPresenter
import su.tagir.apps.radiot.ui.mvp.Status
import su.tagir.apps.radiot.ui.mvp.ViewState
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ArticlesPresenter(private val newsRepository: NewsRepository,
                        private val scheduler: BaseSchedulerProvider,
                        private val router: Router) : BaseListPresenter<Article, ArticlesContract.View>(), ArticlesContract.Presenter {

    private var loadDisposable: Disposable? = null

    private var activeThemeDisposable: Disposable? = null

    private var state = ViewState<List<Article>>(status = Status.SUCCESS)
        set(value) {
            field = value
            view?.updateState(value)
        }

    override fun doOnAttach(view: ArticlesContract.View) {
        super.doOnAttach(view)
        observeArticles()
        loadData(false)
        updateActiveTheme()
    }

    private fun observeArticles() {
        disposables += newsRepository.getArticles()
                .subscribe({
                    state = state.copy(data = it)
                }, { Timber.e(it) })
    }

    override fun loadData(refresh: Boolean) {
        loadDisposable?.dispose()
        loadDisposable = newsRepository.updateArticles()
                .observeOn(scheduler.ui())
                .doOnSubscribe { state = if (refresh) state.copy(status = Status.REFRESHING) else state.copy(status = Status.LOADING) }
                .subscribe({ state = state.copy(status = Status.SUCCESS) },
                        {
                            Timber.e(it)
                            state = state.copy(status = Status.ERROR)
                        })

        disposables += loadDisposable!!
    }

    override fun updateActiveTheme() {
        activeThemeDisposable?.dispose()
        activeThemeDisposable = Observable.interval(1L, 1L, TimeUnit.MINUTES)
                .flatMapSingle { newsRepository.updateActiveArticle().subscribeOn(scheduler.io()) }
                .observeOn(scheduler.ui())
                .retry()
                .subscribe({}, { Timber.e(it) })

        disposables += activeThemeDisposable!!
    }

    override fun onArticleClick(article: Article?) {
        article?.link?.let { link ->
            router.navigateTo(Screens.WebScreen(link))
        }

    }
}
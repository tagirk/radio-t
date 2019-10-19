package su.tagir.apps.radiot.ui.news

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Article
import su.tagir.apps.radiot.model.repository.NewsRepository
import su.tagir.apps.radiot.ui.MainDispatcher
import su.tagir.apps.radiot.ui.mvp.BaseListPresenter
import su.tagir.apps.radiot.ui.mvp.Status

class ArticlesPresenter(private val newsRepository: NewsRepository,
                        private val router: Router,
                        dispatcher: CoroutineDispatcher = MainDispatcher()) : BaseListPresenter<Article, ArticlesContract.View>(dispatcher), ArticlesContract.Presenter {

    private var loadJob: Job? = null

    private var activeThemeJob: Job? = null

    override fun doOnAttach(view: ArticlesContract.View) {
        super.doOnAttach(view)
        observeArticles()
        loadData(false)
        updateActiveTheme()
    }

    override fun doOnDetach() {
        activeThemeJob?.cancel()
    }

    private fun observeArticles() {
        launch {
            newsRepository.getArticles()
                    .collect{list ->
                        state = state.copy(data = list)
                    }
        }
    }

    override fun loadData(pullToRefresh: Boolean) {
        loadJob?.cancel()
        loadJob = launch {
            state = if (pullToRefresh) state.copy(status = Status.REFRESHING) else state.copy(status = Status.LOADING)
            newsRepository.updateArticles()
            state = state.copy(status = Status.SUCCESS)
        }
    }

    override fun updateActiveTheme() {
        activeThemeJob?.cancel()
        activeThemeJob = launch {
            delay(1000L)
                while (true) {
                    newsRepository.updateActiveArticle()
                    delay(1000L)
                }

        }
    }

    override fun showArticle(article: Article) {
        article.link?.let { link ->
            router.navigateTo(Screens.WebScreen(link))
        }

    }
}
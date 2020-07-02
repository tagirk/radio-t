package su.tagir.apps.radiot.ui.news

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.ui.mvp.MainDispatcher
import su.tagir.apps.radiot.ui.mvp.MvpBaseListPresenter
import su.tagir.apps.radiot.ui.mvp.Status
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class NewsPresenter(private val categories: List<String>,
                    private val entryRepository: EntryRepository,
                    dispatcher: CoroutineDispatcher = MainDispatcher(),
                    private val router: Router) : MvpBaseListPresenter<Entry, NewsContract.View>(dispatcher), NewsContract.Presenter {

    private var loadJob: Job? = null

    private val commentatorsContext: CoroutineContext by lazy {
        job + dispatcher + CoroutineExceptionHandler { _, e -> Timber.e(e) }
    }

    override fun doOnAttach(view: NewsContract.View) {
        super.doOnAttach(view)
        observeNews()
    }

    override fun observeNews() {
        launch {
            entryRepository
                    .getEntries(categories = categories)
                    .collect {
                        state = state.copy(data = it)
                    }
        }
    }

    override fun loadCommentators() {
        launch(commentatorsContext) {
            entryRepository.loadCommentators()
        }
    }

    override fun loadData(pullToRefresh: Boolean) {
        loadJob?.cancel()
        loadJob = launch {
            state = if (pullToRefresh) state.copy(status = Status.REFRESHING) else state.copy(status = Status.LOADING)
            entryRepository.refreshEntries(categories, pullToRefresh)
            state = state.copy(status = Status.SUCCESS)
        }
        loadJob?.invokeOnCompletion { t ->
            if (t == null) {
                loadCommentators()
            }
        }
    }

    override fun select(entry: Entry) {
        router.navigateTo(Screens.ContentScreen(entry.title, entry.url))
    }

    override fun openComments(entry: Entry) {
        router.navigateTo(Screens.CommentsScreen(entry))
    }

}
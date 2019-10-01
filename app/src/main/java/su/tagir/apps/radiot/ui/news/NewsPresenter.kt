package su.tagir.apps.radiot.ui.news

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.ui.MainDispatcher
import su.tagir.apps.radiot.ui.mvp.BaseListPresenter
import su.tagir.apps.radiot.ui.mvp.Status

class NewsPresenter(private val entryRepository: EntryRepository,
                    dispatcher: CoroutineDispatcher = MainDispatcher(),
                    private val router: Router) : BaseListPresenter<Entry, NewsContract.View>(dispatcher), NewsContract.Presenter {

    private var loadJob: Job? = null

    override fun doOnAttach(view: NewsContract.View) {
        super.doOnAttach(view)
        observeNews()
        loadData(false)
    }

    override fun observeNews() {
        launch {
            entryRepository
                    .getEntries("news", "info")
                    .collect {
                        state = state.copy(data = it)
                    }
        }
    }

    override fun loadData(pullToRefresh: Boolean) {
        loadJob?.cancel()
        loadJob = launch {
            state = if (pullToRefresh) state.copy(status = Status.REFRESHING) else state.copy(status = Status.LOADING)
            entryRepository.refreshNews()
            state = state.copy(status = Status.SUCCESS)
        }
    }

    override fun select(entry: Entry) {
        router.navigateTo(Screens.ContentScreen(entry.title, entry.url))
    }

    override fun openComments(entry: Entry) {
        router.navigateTo(Screens.CommentsScreen(entry))
    }

}
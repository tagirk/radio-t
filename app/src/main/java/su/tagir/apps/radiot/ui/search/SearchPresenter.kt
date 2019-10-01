package su.tagir.apps.radiot.ui.search

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.ui.MainDispatcher
import su.tagir.apps.radiot.ui.mvp.BaseListPresenter
import su.tagir.apps.radiot.ui.mvp.Status

class SearchPresenter(private val entryRepository: EntryRepository,
                      private val router: Router,
                      dispatcher: CoroutineDispatcher = MainDispatcher()) : BaseListPresenter<Entry, SearchContract.View>(dispatcher), SearchContract.Presenter {


    private var searchJob: Job? = null
    private var dataJob: Job? = null

    private val downloadErrorHandler by lazy {
        CoroutineExceptionHandler { _, exception ->
            view?.showDownloadError(exception.message)
        }
    }

    private var query: String = ""
        set(value) {
            field = value
            observeSearchResults()
        }

    private val itemCount
        get() = state.data?.size ?: 0

    override fun doOnAttach(view: SearchContract.View) {

        launch {
            entryRepository.getRecentSearches()
                    .collect { this@SearchPresenter.view?.showRecentQueries(it) }
        }

        launch {
            while (true) {
                entryRepository.checkDownloadStatus()
                delay(5000L)
            }
        }
    }

    override fun search(query: String) {
        if (this.query == query) {
            return
        }
        this.query = query
        searchJob?.cancel()
        searchJob = launch {
            state = state.copy(status = Status.LOADING)
            entryRepository.search(query)
            state = state.copy(status = Status.SUCCESS, hasNextPage = true)
        }
    }

    override fun update() {
        searchJob?.cancel()
        searchJob = launch {
            state = state.copy(status = Status.REFRESHING)
            entryRepository.search(query)
            state = state.copy(status = Status.SUCCESS, hasNextPage = true)
        }
    }

    override fun loadMore() {
        if (searchJob != null && searchJob!!.isActive) {
            return
        }
        searchJob = launch {
            state = state.copy(status = Status.LOADING_MORE)
            val hasNext = entryRepository.searchNextPage(query, itemCount)
            state = state.copy(status = Status.SUCCESS, hasNextPage = hasNext)
        }
    }

    override fun removeQuery(query: String) {
        launch {
            entryRepository.removeQuery(query)
        }
    }

    override fun download(entry: Entry) {
        launch(downloadErrorHandler) {
            entryRepository
                    .startDownload(entry.audioUrl)
        }
    }


    private fun observeSearchResults() {
        dataJob?.cancel()
        dataJob = launch {
            entryRepository.getForQuery(query)
                    .collect { state = state.copy(data = it) }
        }
    }

    override fun loadData(pullToRefresh: Boolean) {

    }

    override fun openComments(entry: Entry) {
        router.navigateTo(Screens.CommentsScreen(entry = entry))
    }


    override fun select(entry: Entry) {
        entryRepository.play(entry)
    }

    override fun remove(entry: Entry) {
        launch(downloadErrorHandler) {
            entryRepository.deleteFile(entry.downloadId)
        }
    }

    override fun exit() {
        router.exit()
    }

}
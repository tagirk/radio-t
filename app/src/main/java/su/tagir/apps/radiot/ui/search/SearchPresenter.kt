package su.tagir.apps.radiot.ui.search

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.BaseListPresenter
import su.tagir.apps.radiot.ui.mvp.Status
import su.tagir.apps.radiot.ui.mvp.ViewState
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SearchPresenter(private val entryRepository: EntryRepository,
                      private val scheduler: BaseSchedulerProvider,
                      private val router: Router) : BaseListPresenter<Entry, SearchContract.View>(), SearchContract.Presenter {

    private var searchDisposable: Disposable? = null
    private var dataDisposable: Disposable? = null

    private var entryForDownload: Entry? = null

    private var query: String = ""
        set(value) {
            field = value
            observeSearchResults()
        }

    private var state = ViewState<List<Entry>>(status = Status.SUCCESS)
        set(value) {
            field = value
            view?.updateState(value)
        }

    private val itemCount
        get() = state.data?.size ?: 0

    override fun doOnAttach(view: SearchContract.View) {

        val recentSearchesDisposable = entryRepository.getRecentSearches()
                .subscribe({
                    this.view?.showRecentQueries(it)
                }, {
                    Timber.e(it)
                })
        disposables += recentSearchesDisposable

        val intervalDisposable =
                Observable
                        .interval(0L, 5L, TimeUnit.SECONDS)
                        .subscribeOn(scheduler.computation())
                        .observeOn(scheduler.io())
                        .subscribe({
                            entryRepository.checkDownloadStatus()
                        }, {
                            Timber.e(it)
                        })

        disposables += intervalDisposable
    }

    override fun search(query: String) {
        if (this.query == query) {
            return
        }
        this.query = query
        searchDisposable?.dispose()
        searchDisposable = entryRepository.search(query)
                .observeOn(scheduler.ui())
                .doOnSubscribe { state = state.copy(status = Status.LOADING) }
                .subscribe({
                    state = state.copy(status = Status.SUCCESS, hasNextPage = true)
                }, {
                    Timber.e(it)
                    state = state.copy(status = Status.ERROR, errorMessage = it.localizedMessage)
                })
    }

    override fun update() {
        searchDisposable?.dispose()
        searchDisposable = entryRepository.search(query)
                .observeOn(scheduler.ui())
                .doOnSubscribe { state = state.copy(status = Status.REFRESHING) }
                .subscribe({
                    state = state.copy(status = Status.SUCCESS, hasNextPage = true)
                }, {
                    Timber.e(it)
                    state = state.copy(status = Status.ERROR, errorMessage = it.localizedMessage)
                })
        disposables += searchDisposable!!
    }

    override fun loadMore() {
        if (searchDisposable != null && !searchDisposable!!.isDisposed) {
            return
        }
        searchDisposable = entryRepository.searchNextPage(query, itemCount)
                .observeOn(scheduler.ui())
                .doOnSubscribe { state = state.copy(status = Status.LOADING_MORE) }
                .subscribe({
                    state = state.copy(status = Status.SUCCESS, hasNextPage = it)
                }, {
                    Timber.e(it)
                    state = state.copy(status = Status.ERROR, errorMessage = it.localizedMessage)
                })
        disposables += searchDisposable!!
    }

    override fun removeQuery(query: String) {
        entryRepository.removeQuery(query)
    }

    override fun download() {
        entryForDownload?.let { entry ->
            addDisposable(entryRepository
                    .startDownload(entry.audioUrl)
                    .subscribeOn(scheduler.io())
                    .observeOn(scheduler.ui())
                    .subscribe({}, { t ->
                        Timber.e(t)
                        view?.showDownloadError(t.localizedMessage)
                    }))
        }
    }


    private fun observeSearchResults() {
        dataDisposable?.dispose()
        dataDisposable = entryRepository.getForQuery(query)
                .observeOn(scheduler.ui())
                .subscribe({
                    state = state.copy(data = it)
                }, { Timber.e(it) })
        disposables += dataDisposable!!
    }

    override fun loadData(refresh: Boolean) {

    }

    override fun onCommentClick(entry: Entry) {
        router.navigateTo(Screens.CommentsScreen(entry = entry))
    }

    override fun onDownloadClick(entry: Entry) {
        entryForDownload = entry
        view?.download()
    }

    override fun onEntryClick(entry: Entry) {
        entryRepository.play(entry)
    }

    override fun onRemoveClick(entry: Entry) {
        disposables += entryRepository.deleteFile(entry.downloadId)
                .observeOn(scheduler.ui())
                .subscribe({}, { e ->
                    Timber.e(e)
                    view?.showDownloadError(e.localizedMessage)
                })
    }

}
package su.tagir.apps.radiot.ui.search

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.viewmodel.ListViewModel
import su.tagir.apps.radiot.ui.viewmodel.State
import su.tagir.apps.radiot.ui.viewmodel.Status
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SearchViewModel @Inject constructor(private val entryRepository: EntryRepository,
                                          private val router: Router,
                                          schedulerProvider: BaseSchedulerProvider) : ListViewModel<Entry>(schedulerProvider) {

    private var dataDisposable: Disposable? = null
    private var loadDisposable: Disposable? = null

    private val recentSearches = MutableLiveData<List<String>>()

    private var query: String = ""
        set(value) {
            field = value
            observeSearchResults()
            loadData()
        }


    private lateinit var intervalDisposable: Disposable

    init {

        disposable += entryRepository.getRecentSearches()
                .subscribe({ recentSearches.postValue(it) },
                        { Timber.e(it) })
    }

    fun getRecentSearches(): LiveData<List<String>> = recentSearches

    fun search(query: String) {
        if (this.query == query) {
            return
        }
        this.query = query
    }

    private fun observeSearchResults() {
        dataDisposable?.dispose()
        dataDisposable = entryRepository.getForQuery(query)
                .observeOn(scheduler.ui())
                .subscribe({
                    val newState = state.value?.copy(data = it)
                    state.value = newState
                }, { Timber.e(it) })
        disposable += dataDisposable!!
    }

    override fun loadData() {
        loadDisposable?.dispose()
        loadDisposable = entryRepository.search(query)
                .observeOn(scheduler.ui())
                .doOnSubscribe { state.value = if (state.value == null) State(Status.LOADING) else state.value?.copy(Status.LOADING) }
                .subscribe({ state.value = state.value?.copy(status = Status.SUCCESS, hasNextPage = true) }, {
                    Timber.e(it)
                    state.value = state.value?.copy(status = Status.ERROR)
                })

        disposable += loadDisposable!!
    }

    override fun requestUpdates() {
        loadDisposable?.dispose()
        loadDisposable = entryRepository.search(query)
                .observeOn(scheduler.ui())
                .doOnSubscribe { state.value = state.value?.copy(status = Status.REFRESHING) }
                .subscribe({ state.value = state.value?.copy(status = Status.SUCCESS, hasNextPage = true) }, {
                    Timber.e(it)
                    state.value = state.value?.copy(status = Status.ERROR)
                })
        disposable += loadDisposable!!
    }

    override fun loadMore() {
        if (loadDisposable != null && !loadDisposable!!.isDisposed) {
            return
        }
        state.value = state.value?.copy(status = Status.LOADING_MORE)
        loadDisposable = entryRepository.searchNextPage(query, itemCount)
                .observeOn(scheduler.ui())
                .subscribe({ state.value = state.value?.copy(status = Status.SUCCESS, hasNextPage = it) }, {
                    Timber.e(it)
                    state.value = state.value?.copy(status = Status.ERROR)
                })
        disposable += loadDisposable!!
    }

    private val itemCount
        get() = state.value?.data?.size ?: 0

    fun startStatusTimer() {
        intervalDisposable =
                Observable
                        .interval(0L, 5L, TimeUnit.SECONDS)
                        .subscribeOn(scheduler.computation())
                        .observeOn(scheduler.diskIO())
                        .subscribe({ entryRepository.checkDownloadStatus() }, { Timber.e(it) })

        disposable += intervalDisposable
    }

    fun stopStatusTimer() {
        intervalDisposable.dispose()
    }

    fun onEntryCkick(entry: Entry) {
        if (entry.audioUrl != null) {
            entryRepository.play(entry)
        } else {
            router.navigateTo(Screens.WEB_SCREEN, entry.url)
        }
    }

    fun onDownloadClick(entry: Entry) {
        addDisposable(entryRepository
                .startDownload(entry.audioUrl)
                .subscribeOn(scheduler.io())
                .subscribe({}, { t -> Timber.e(t) }))
    }

    fun onRemoveClick(entry: Entry) {
        addDisposable(entryRepository
                .deleteFile(entry.downloadId)
                .subscribeOn(scheduler.io())
                .subscribe({}, { t -> Timber.e(t) }))
    }

    fun openWebSite(entry: Entry) {
        router.navigateTo(Screens.WEB_SCREEN, entry.url)
    }

    fun openChatLog(entry: Entry) {
        router.navigateTo(Screens.WEB_SCREEN, entry.chatUrl)
    }

    fun removeQuery(query: String?) {
        entryRepository.removeQuery(query)
    }

    fun onBackClick() {
        router.exit()
    }
}
package su.tagir.apps.radiot.ui.search

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.model.repository.EntryRepository.Companion.PAGE_SIZE
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.viewmodel.BaseViewModel
import su.tagir.apps.radiot.ui.viewmodel.ViewModelState
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SearchViewModel
@Inject constructor(private val entryRepository: EntryRepository,
                    private val router: Router,
                    schedulerProvider: BaseSchedulerProvider) : BaseViewModel(schedulerProvider) {


    private val query = MutableLiveData<String>()
    val data: LiveData<List<Entry>>
    val state = MutableLiveData<ViewModelState?>()
    val resentSearches = LivePagedListBuilder(entryRepository.getRecentSearches(), PAGE_SIZE).build()

    private lateinit var intervalDisposable: Disposable

    init {
        data = Transformations
                .switchMap(query, { query -> entryRepository.getForQuery(query) })
    }


    fun search(query: String?) {
        if (query.isNullOrBlank()) {
            return
        }
        this.query.value = query
        state.postValue(ViewModelState.LOADING)
        addDisposable(entryRepository
                .search(query!!)
                .subscribe({ state.postValue(ViewModelState.COMPLETE) },
                        { state.postValue(ViewModelState.error(it.message)) }))
    }

    fun searchMore() {
        if (state.value?.loading == true || state.value?.loadingMore == true) {
            return
        }
        state.value = ViewModelState.LOADING_MORE
        addDisposable(entryRepository
                .searchNextPage(query.value!!)
                .subscribe({ state.postValue(ViewModelState.COMPLETE) },
                        { state.postValue(ViewModelState.error(it.message)) }))
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

    fun removeQuery(query: String?) {
        entryRepository.removeQuery(query)
    }

    fun onBackClick() {
        router.exit()
    }

    fun onResume() {
        intervalDisposable =
                Observable
                        .interval(0L, 5L, TimeUnit.SECONDS)
                        .subscribe({ entryRepository.checkDownloadStatus() }, { Timber.e(it) })

        addDisposable(intervalDisposable)
    }

    fun onPause() {
        intervalDisposable.dispose()
    }
}
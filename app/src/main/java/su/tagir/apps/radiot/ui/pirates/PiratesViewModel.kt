package su.tagir.apps.radiot.ui.pirates

import android.arch.lifecycle.LiveData
import android.support.annotation.VisibleForTesting
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.common.SingleLiveEvent
import su.tagir.apps.radiot.ui.viewmodel.ListViewModel
import su.tagir.apps.radiot.ui.viewmodel.State
import su.tagir.apps.radiot.ui.viewmodel.Status
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PiratesViewModel @Inject constructor(
        private val entryRepository: EntryRepository,
        scheduler: BaseSchedulerProvider) : ListViewModel<Entry>(scheduler) {

    private val downloadError = SingleLiveEvent<String>()

    private var loadDisposable: Disposable? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var intervalDisposable: Disposable? = null


    init {
        disposable += entryRepository
                .getEntries("pirates")
                .subscribe({
                    val newState = if (state.value == null)
                        State(Status.SUCCESS, it)
                    else
                        state.value?.copy(data = it)

                    state.value = newState
                }, { Timber.e(it) })

        loadData()
    }

    override fun loadData() {
        loadDisposable?.dispose()
        loadDisposable = entryRepository.refreshPirates()
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
        loadDisposable = entryRepository.refreshPirates()
                .observeOn(scheduler.ui())
                .doOnSubscribe { state.value = state.value?.copy(status = Status.REFRESHING) }
                .subscribe({ state.value = state.value?.copy(status = Status.SUCCESS) },
                        {
                            Timber.e(it)
                            state.value = state.value?.copy(status = Status.ERROR)
                        })

        disposable += loadDisposable!!
    }

    fun startStatusTimer() {
        intervalDisposable =
                Observable
                        .interval(0L, 5L, TimeUnit.SECONDS)
                        .subscribeOn(scheduler.computation())
                        .observeOn(scheduler.diskIO())
                        .subscribe({ entryRepository.checkDownloadStatus() }, { Timber.e(it) })

        addDisposable(intervalDisposable!!)
    }

    fun stopStatusTimer() {
        intervalDisposable?.dispose()
    }

    fun onDownloadClick(entry: Entry) {
        addDisposable(entryRepository
                .startDownload(entry.audioUrl)
                .subscribeOn(scheduler.diskIO())
                .observeOn(scheduler.ui())
                .subscribe({}, { t ->
                    Timber.e(t)
                    downloadError.setValue(t.message)
                }))
    }

    fun onRemoveClick(entry: Entry) {
        addDisposable(entryRepository
                .deleteFile(entry.downloadId)
                .subscribeOn(scheduler.diskIO())
                .observeOn(scheduler.ui())
                .subscribe({}, { t ->
                    Timber.e(t)
                    downloadError.setValue(t.message)
                }))
    }

    fun getDownloadError(): LiveData<String> = downloadError
}
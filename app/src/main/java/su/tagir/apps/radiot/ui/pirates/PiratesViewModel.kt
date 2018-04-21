package su.tagir.apps.radiot.ui.pirates

import android.arch.paging.LivePagedListBuilder
import android.support.annotation.VisibleForTesting
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.common.SingleLiveEvent
import su.tagir.apps.radiot.ui.viewmodel.ListViewModel
import su.tagir.apps.radiot.ui.viewmodel.ViewModelState
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PiratesViewModel @Inject constructor(
        private val entryRepository:EntryRepository,
        scheduler:BaseSchedulerProvider):ListViewModel<Entry>(scheduler) {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var intervalDisposable: Disposable? = null

    val error = SingleLiveEvent<String>()

    override fun getData() = LivePagedListBuilder(entryRepository.getEntries("pirates"), EntryRepository.PAGE_SIZE).build()

    override fun requestUpdates() {
        addDisposable(entryRepository.refreshPirates()
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribe({ state.postValue(ViewModelState.COMPLETE) },
                        { t ->
                            Timber.e(t)
                            if (state.value?.refreshing == true) {
                                error.value = t.message
                            }
                            state.postValue(ViewModelState.error(t.message))
                        }))
    }

    fun startStatusTimer() {
        intervalDisposable =
                Observable
                        .interval(0L, 5L, TimeUnit.SECONDS)
                        .subscribeOn(scheduler.computation())
                        .subscribe({ entryRepository.checkDownloadStatus() }, { Timber.e(it) })

        addDisposable(intervalDisposable!!)
    }

    fun stopStatusTimer() {
        intervalDisposable?.dispose()
    }

    fun onDownloadClick(entry: Entry) {
        addDisposable(entryRepository
                .startDownload(entry.audioUrl)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribe({}, { t ->
                    Timber.e(t)
                    error.setValue(t.message)
                }))
    }

    fun onRemoveClick(entry: Entry) {
        addDisposable(entryRepository
                .deleteFile(entry.downloadId)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribe({}, { t ->
                    Timber.e(t)
                    error.setValue(t.message)
                }))
    }
}
package su.tagir.apps.radiot.ui.podcasts.downloaded

import android.arch.lifecycle.LiveData
import io.reactivex.rxkotlin.plusAssign
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.common.SingleLiveEvent
import su.tagir.apps.radiot.ui.viewmodel.ListViewModel
import su.tagir.apps.radiot.ui.viewmodel.State
import su.tagir.apps.radiot.ui.viewmodel.Status
import timber.log.Timber
import javax.inject.Inject

class DownloadedPodcastsViewModel @Inject constructor(scheduler: BaseSchedulerProvider,
                                                      private val entryRepository: EntryRepository) : ListViewModel<Entry>(scheduler) {

    private val error = SingleLiveEvent<String>()

    init {
        disposable += entryRepository
                .getDownloadedEntries("podcast")
                .subscribe({
                    Timber.d("entries: $it")
                    val newState = if (state.value == null)
                        State(Status.SUCCESS, it)
                    else
                        state.value?.copy(data = it)

                    state.value = newState
                }, { Timber.e(it) })
    }

    override fun loadData() {

    }

    fun onRemoveClick(entry: Entry) {
        disposable+=entryRepository
                .deleteFile(entry.downloadId)
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribe({}, { t ->
                    Timber.e(t)
                   error.setValue(t.message)
                })
    }

    fun error(): LiveData<String> = error

}
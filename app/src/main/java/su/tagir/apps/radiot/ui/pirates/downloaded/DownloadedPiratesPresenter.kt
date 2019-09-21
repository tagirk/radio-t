package su.tagir.apps.radiot.ui.pirates.downloaded

import io.reactivex.rxkotlin.plusAssign
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.BaseListPresenter
import timber.log.Timber

class DownloadedPiratesPresenter(private val entryRepository: EntryRepository,
                                 private val scheduler: BaseSchedulerProvider) :
        BaseListPresenter<Entry, DownloadedPiratesContract.View>(), DownloadedPiratesContract.Presenter {


    override fun doOnAttach(view: DownloadedPiratesContract.View) {
        observePodcasts()
    }

    override fun loadData(pullToRefresh: Boolean) {

    }

    override fun select(entry: Entry) {
        entryRepository.play(entry)
    }


    override fun remove(entry: Entry) {
        disposables += entryRepository.deleteFile(entry.downloadId)
                .subscribe({}, { e ->
                    Timber.e(e)
                    view?.showRemoveError(e.message)
                })
    }

    private fun observePodcasts() {
        disposables +=
                entryRepository
                        .getDownloadedEntries("pirates")
                        .observeOn(scheduler.ui())
                        .subscribe({ data ->
                            state = state.copy(data = data)
                        }, { Timber.e(it) })

    }
}
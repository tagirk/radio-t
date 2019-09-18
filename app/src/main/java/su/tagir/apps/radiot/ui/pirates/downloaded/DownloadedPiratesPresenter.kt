package su.tagir.apps.radiot.ui.pirates.downloaded

import io.reactivex.rxkotlin.plusAssign
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.BaseListPresenter
import su.tagir.apps.radiot.ui.mvp.Status
import su.tagir.apps.radiot.ui.mvp.ViewState
import timber.log.Timber

class DownloadedPiratesPresenter(private val entryRepository: EntryRepository,
                                 private val scheduler: BaseSchedulerProvider) :
        BaseListPresenter<Entry, DownloadedPiratesContract.View>(), DownloadedPiratesContract.Presenter {


    private var state = ViewState<List<Entry>>(status = Status.SUCCESS)
        set(value) {
            field = value
            view?.updateState(value)
        }

    override fun doOnAttach(view: DownloadedPiratesContract.View) {
        observePodcasts()
    }

    override fun loadData(refresh: Boolean) {

    }

    override fun onEntryClick(entry: Entry) {
        entryRepository.play(entry)
    }

    override fun onDownloadClick(entry: Entry) {

    }

    override fun onRemoveClick(entry: Entry) {
        disposables += entryRepository.deleteFile(entry.downloadId)
                .subscribe({}, { e ->
                    Timber.e(e)
                    view?.showRemoveError(e.localizedMessage)
                })
    }

    override fun onCommentClick(entry: Entry) {

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
package su.tagir.apps.radiot.ui.pirates.downloaded

import io.reactivex.rxkotlin.plusAssign
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.BasePresenter
import su.tagir.apps.radiot.ui.mvp.Status
import su.tagir.apps.radiot.ui.mvp.ViewState
import timber.log.Timber
import java.util.concurrent.TimeUnit

class DownloadedPiratesPresenter(private val entryRepository: EntryRepository,
                                 private val scheduler: BaseSchedulerProvider) :
        BasePresenter<DownloadedPiratesContract.View>(), DownloadedPiratesContract.Presenter {

    private var state = ViewState<List<Entry>>(status = Status.SUCCESS)
        set(value) {
            field = value
            view?.updateState(value)
        }

    override fun doOnAttach(view: DownloadedPiratesContract.View) {
        observePodcasts()
        observeClickEvents(view)
    }

    private fun observePodcasts() {
        disposables +=
                entryRepository
                        .getDownloadedEntries("pirates")
                        .subscribe({ data ->
                            state = state.copy(data = data)
                        }, { Timber.e(it) })

    }

    private fun observeClickEvents(v: DownloadedPiratesContract.View) {
        disposables += v.entryClickRequests()
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe({ entry -> entryRepository.play(entry) },
                        { e ->
                            Timber.e(e)
                        })

        disposables += v.removeClickRequests()
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(scheduler.io())
                .flatMapCompletable { entry -> entryRepository.deleteFile(entry.downloadId) }
                .subscribe({}, { e ->
                    Timber.e(e)
                    view?.showRemoveError(e.localizedMessage)
                })

    }

}
package su.tagir.apps.radiot.ui.pirates

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.BaseListPresenter
import su.tagir.apps.radiot.ui.mvp.Status
import su.tagir.apps.radiot.ui.mvp.ViewState
import timber.log.Timber
import java.util.concurrent.TimeUnit

class PiratesPresenter(private val entryRepository: EntryRepository,
                       private val scheduler: BaseSchedulerProvider) : BaseListPresenter<Entry, PiratesContract.View>(), PiratesContract.Presenter {

    private var loadDisposable: Disposable? = null
    private var entryForDownload: Entry? = null

    private var state = ViewState<List<Entry>>(status = Status.SUCCESS)
        set(value) {
            field = value
            view?.updateState(value)
        }

    override fun doOnAttach(view: PiratesContract.View) {
        observePodcasts()
        startStatusTimer()
    }


    private fun observePodcasts() {
        disposables += entryRepository
                .getEntries("pirates")
                .subscribe({
                    state.copy(data = it)
                }, { Timber.e(it) })
    }

    override fun loadData(refresh: Boolean) {
        loadDisposable?.dispose()
        loadDisposable = entryRepository.refreshPirates()
                .observeOn(scheduler.ui())
                .doOnSubscribe { state.copy(status = Status.REFRESHING) }
                .subscribe({ state.copy(status = Status.SUCCESS) },
                        {
                            Timber.e(it)
                            state.copy(status = Status.ERROR)
                        })

        disposables += loadDisposable!!
    }

    private fun startStatusTimer() {
       disposables +=
                Observable
                        .interval(0L, 5L, TimeUnit.SECONDS)
                        .subscribeOn(scheduler.computation())
                        .observeOn(scheduler.io())
                        .subscribe({ entryRepository.checkDownloadStatus() }, { Timber.e(it) })
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

    override fun onEntryClick(entry: Entry) {
        entryRepository.play(entry)
    }

    override fun onDownloadClick(entry: Entry) {
        entryForDownload = entry
        view?.download()
    }

    override fun onRemoveClick(entry: Entry) {
        disposables +=  entryRepository.deleteFile(entry.downloadId)
                .observeOn(scheduler.ui())
                .subscribe({}, { e ->
                    Timber.e(e)
                    view?.showDownloadError(e.localizedMessage)
                })
    }

    override fun onCommentClick(entry: Entry) {

    }
}
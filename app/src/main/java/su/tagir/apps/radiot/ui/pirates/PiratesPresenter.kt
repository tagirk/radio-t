package su.tagir.apps.radiot.ui.pirates

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.BaseListPresenter
import su.tagir.apps.radiot.ui.mvp.Status
import timber.log.Timber
import java.util.concurrent.TimeUnit

class PiratesPresenter(private val entryRepository: EntryRepository,
                       private val scheduler: BaseSchedulerProvider) : BaseListPresenter<Entry, PiratesContract.View>(), PiratesContract.Presenter {

    private var loadDisposable: Disposable? = null

    override fun doOnAttach(view: PiratesContract.View) {
        observePodcasts()
        loadData(false)
        startStatusTimer()
    }

    private fun observePodcasts() {
        disposables += entryRepository
                .getEntries("pirates")
                .subscribe({
                    state = state.copy(data = it)
                }, { Timber.e(it) })
    }

    override fun loadData(pullToRefresh: Boolean) {
        loadDisposable?.dispose()
        loadDisposable = entryRepository.refreshPirates()
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .doOnSubscribe { state = state.copy(status = if(pullToRefresh) Status.REFRESHING else Status.LOADING) }
                .subscribe({ state = state.copy(status = Status.SUCCESS) },
                        {
                            Timber.e(it)
                            state = state.copy(status = Status.ERROR)
                        })

        disposables += loadDisposable!!
    }

    private fun startStatusTimer() {
       disposables +=
                Observable
                        .interval(0L, 5L, TimeUnit.SECONDS)
                        .observeOn(scheduler.io())
                        .subscribe({ entryRepository.checkDownloadStatus() }, { Timber.e(it) })
    }


    override fun download(entry: Entry) {
            addDisposable(entryRepository
                    .startDownload(entry.audioUrl)
                    .subscribeOn(scheduler.io())
                    .observeOn(scheduler.ui())
                    .subscribe({}, { t ->
                        Timber.e(t)
                        view?.showDownloadError(t.message)
                    }))
    }

    override fun select(entry: Entry) {
        entryRepository.play(entry)
    }

    override fun remove(entry: Entry) {
        disposables +=  entryRepository.deleteFile(entry.downloadId)
                .observeOn(scheduler.ui())
                .subscribe({}, { e ->
                    Timber.e(e)
                    view?.showDownloadError(e.localizedMessage)
                })
    }
}
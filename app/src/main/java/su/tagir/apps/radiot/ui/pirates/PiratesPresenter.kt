package su.tagir.apps.radiot.ui.pirates

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.BasePresenter
import su.tagir.apps.radiot.ui.mvp.Status
import su.tagir.apps.radiot.ui.mvp.ViewState
import timber.log.Timber
import java.util.concurrent.TimeUnit

class PiratesPresenter(private val entryRepository: EntryRepository,
                       private val scheduler: BaseSchedulerProvider) : BasePresenter<PiratesContract.View>(), PiratesContract.Presenter {

    private var loadDisposable: Disposable? = null
    private var entryForDownload: Entry? = null

    private var state = ViewState<List<Entry>>(status = Status.SUCCESS)
        set(value) {
            field = value
            view?.updateState(value)
        }

    override fun doOnAttach(view: PiratesContract.View) {
        observePodcasts()
        update()
        startStatusTimer()
        observeClickEvents(view)
    }


    private fun observePodcasts() {
        disposables += entryRepository
                .getEntries("pirates")
                .subscribe({
                    state.copy(data = it)
                }, { Timber.e(it) })
    }

    override fun update() {
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

    private fun observeClickEvents(v: PiratesContract.View) {
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
                    view?.showDownloadError(e.localizedMessage)
                })

        disposables += v.downloadClickRequests()
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe { view?.download() }
    }
}
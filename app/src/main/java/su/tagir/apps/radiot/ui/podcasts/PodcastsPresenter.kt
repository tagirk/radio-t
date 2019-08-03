package su.tagir.apps.radiot.ui.podcasts

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.BasePresenter
import su.tagir.apps.radiot.ui.mvp.Status
import su.tagir.apps.radiot.ui.mvp.ViewState
import timber.log.Timber
import java.util.concurrent.TimeUnit

class PodcastsPresenter(private val entryRepository: EntryRepository,
                        private val scheduler: BaseSchedulerProvider,
                        private val router: Router) : BasePresenter<PodcastsContract.View>(), PodcastsContract.Presenter {

    private var intervalDisposable: Disposable? = null
    private var loadDisposable: Disposable? = null

    private var entryForDownload: Entry? = null

    private var state = ViewState<List<Entry>>(status = Status.SUCCESS)
        set(value) {
            field = value
            view?.updateState(value)
        }

    override fun doOnAttach(view: PodcastsContract.View) {
        observePodcasts()
        update()
        startStatusTimer()
        observeClickEvents(view)
    }


    private fun observePodcasts() {
        addDisposable(
                entryRepository
                        .getEntries("podcast")
                        .subscribe({ data ->
                            state = state.copy(data = data)
                            view?.updateState(state)
                        }, { Timber.e(it) })
        )
    }

    private fun startStatusTimer() {
        intervalDisposable =
                Observable
                        .interval(0L, 5L, TimeUnit.SECONDS)
                        .subscribeOn(scheduler.computation())
                        .observeOn(scheduler.io())
                        .subscribe({ entryRepository.checkDownloadStatus() }, { Timber.e(it) })

        addDisposable(intervalDisposable!!)
    }

    override fun update() {
        loadDisposable?.dispose()
        loadDisposable = entryRepository
                .refreshPodcasts()
                .observeOn(scheduler.ui())
                .doOnSubscribe { state = state.copy(status = Status.LOADING) }
                .subscribe({
                    state = state.copy(status = Status.SUCCESS)
                }, { e ->
                    Timber.e(e)
                    state = state.copy(status = Status.ERROR, errorMessage = e.localizedMessage)
                })
        disposables += loadDisposable!!
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

    private fun observeClickEvents(v: PodcastsContract.View) {
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

        disposables += v.commentClickRequests()
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe { router.navigateTo(Screens.CommentsScreen(entry = it)) }

        disposables += v.downloadClickRequests()
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe { view?.download() }


    }
}
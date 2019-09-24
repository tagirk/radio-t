package su.tagir.apps.radiot.ui.podcasts

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.BaseListPresenter
import su.tagir.apps.radiot.ui.mvp.Status
import timber.log.Timber
import java.util.concurrent.TimeUnit

class PodcastsPresenter(private val entryRepository: EntryRepository,
                        private val scheduler: BaseSchedulerProvider,
                        private val router: Router) : BaseListPresenter<Entry, PodcastsContract.View>(), PodcastsContract.Presenter {

    private var intervalDisposable: Disposable? = null
    private var loadDisposable: Disposable? = null

    override fun doOnAttach(view: PodcastsContract.View) {
        observePodcasts()
        loadData(false)
        startStatusTimer()
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
                        .observeOn(scheduler.io())
                        .subscribe({ entryRepository.checkDownloadStatus() }, { Timber.e(it) })

        addDisposable(intervalDisposable!!)
    }

    override fun loadData(pullToRefresh: Boolean) {
        loadDisposable?.dispose()
        loadDisposable = entryRepository
                .refreshPodcasts()
                .observeOn(scheduler.ui())
                .doOnSubscribe { state = if (pullToRefresh) state.copy(status = Status.REFRESHING) else state.copy(status = Status.LOADING) }
                .subscribe({
                    state = state.copy(status = Status.SUCCESS)
                }, { e ->
                    Timber.e(e)
                    state = state.copy(status = Status.ERROR, errorMessage = e.localizedMessage)
                })
        disposables += loadDisposable!!
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
        disposables += entryRepository.deleteFile(entry.downloadId)
                .observeOn(scheduler.ui())
                .subscribe({}, { e ->
                    Timber.e(e)
                    view?.showDownloadError(e.message)
                })
    }

    override fun openComments(entry: Entry) {
        router.navigateTo(Screens.CommentsScreen(entry = entry))
    }
}
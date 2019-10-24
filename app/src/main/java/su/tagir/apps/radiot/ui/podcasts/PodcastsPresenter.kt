package su.tagir.apps.radiot.ui.podcasts

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.ui.mvp.BaseListPresenter
import su.tagir.apps.radiot.ui.mvp.MainDispatcher
import su.tagir.apps.radiot.ui.mvp.Status
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class PodcastsPresenter(private val entryRepository: EntryRepository,
                        private val router: Router,
                        dispatcher: CoroutineDispatcher = MainDispatcher()) : BaseListPresenter<Entry, PodcastsContract.View>(dispatcher), PodcastsContract.Presenter {

    private var loadJob: Job? = null

    private val downloadContext: CoroutineContext by lazy {
        job + dispatcher + CoroutineExceptionHandler { _, exception -> view?.showDownloadError(exception.message) }
    }

    private val commentatorsContext: CoroutineContext by lazy {
        job + dispatcher + CoroutineExceptionHandler { _, e -> Timber.e(e) }
    }

    override fun doOnAttach(view: PodcastsContract.View) {
        observePodcasts()
        loadData(false)
        startStatusTimer()
    }

    private fun observePodcasts() {
        launch {
            entryRepository
                    .getEntries("podcast", "prep")
                    .collect { data ->
                        state = state.copy(data = data)
                    }
        }
    }

    private fun startStatusTimer() {
        launch {
            while (true) {
                entryRepository.checkDownloadStatus()
                delay(5000L)
            }
        }
    }

    override fun loadData(pullToRefresh: Boolean) {
        loadJob?.cancel()
        loadJob = launch {
            state = if (pullToRefresh) state.copy(status = Status.REFRESHING) else state.copy(status = Status.LOADING)
            entryRepository.refreshPodcasts()
            state = state.copy(status = Status.SUCCESS)
        }

        loadJob?.invokeOnCompletion { t ->
            if (t == null) {
                loadCommentators()
            }
        }
    }

    override fun loadCommentators() {
        launch(commentatorsContext) {
            entryRepository.loadCommentators()
        }
    }

    override fun download(entry: Entry) {
        launch(downloadContext) {
            entryRepository
                    .startDownload(entry.audioUrl)
        }
    }

    override fun select(entry: Entry) {
        entryRepository.play(entry)
    }

    override fun remove(entry: Entry) {
        launch(downloadContext) {
            entryRepository.deleteFile(entry.downloadId)
        }
    }

    override fun openComments(entry: Entry) {
        router.navigateTo(Screens.CommentsScreen(entry = entry))
    }
}
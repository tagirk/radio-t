package su.tagir.apps.radiot.ui.pirates

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.ui.MainDispatcher
import su.tagir.apps.radiot.ui.mvp.BaseListPresenter
import su.tagir.apps.radiot.ui.mvp.Status

class PiratesPresenter(private val entryRepository: EntryRepository,
                       dispatcher: CoroutineDispatcher = MainDispatcher()) : BaseListPresenter<Entry, PiratesContract.View>(dispatcher), PiratesContract.Presenter {

    private var loadJob: Job? = null

    private val downloadErrorHandler by lazy {
        CoroutineExceptionHandler { _, exception ->
            view?.showDownloadError(exception.message)
        }
    }

    override fun doOnAttach(view: PiratesContract.View) {
        observePodcasts()
        loadData(false)
        startStatusTimer()
    }

    private fun observePodcasts() {
        launch {
            entryRepository
                    .getEntries("pirates")
                    .collect {
                        state = state.copy(data = it)
                    }
        }
    }

    override fun loadData(pullToRefresh: Boolean) {
        loadJob?.cancel()
        state = state.copy(status = if (pullToRefresh) Status.REFRESHING else Status.LOADING)
        loadJob = launch {
            entryRepository.refreshPirates()
            state = state.copy(status = Status.SUCCESS)
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

    override fun download(entry: Entry) {
        launch(downloadErrorHandler) {
            entryRepository
                    .startDownload(entry.audioUrl)
        }
    }

    override fun select(entry: Entry) {
        entryRepository.play(entry)
    }

    override fun remove(entry: Entry) {
        launch(downloadErrorHandler) {
            entryRepository
                    .deleteFile(entry.downloadId)
        }
    }
}
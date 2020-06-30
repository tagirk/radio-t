package su.tagir.apps.radiot.ui.podcasts.downloaded

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.ui.mvp.MainDispatcher
import su.tagir.apps.radiot.ui.mvp.MvpBaseListPresenter

class DownloadedPodcastsPresenter(private val entryRepository: EntryRepository,
                                  private val router: Router,
                                  dispatcher: CoroutineDispatcher = MainDispatcher()):
        MvpBaseListPresenter<Entry, DownloadedPodcastsContract.View>(dispatcher),
        DownloadedPodcastsContract.Presenter {

    private val deleterErrorHandler by lazy {
        CoroutineExceptionHandler { _, exception ->
            ifViewAttached({v -> v.showRemoveError(exception.message)})
        }
    }

    override fun doOnAttach(view: DownloadedPodcastsContract.View) {
        loadData(false)
    }


    override fun loadData(pullToRefresh: Boolean) {
        launch {
            entryRepository
                    .getDownloadedEntries(listOf("podcast"))
                    .collect { data ->
                        state = state.copy(data = data) }
        }
    }

    override fun select(entry: Entry) {
        entryRepository.play(entry)
    }


    override fun remove(entry: Entry) {
        launch(deleterErrorHandler) {  entryRepository.deleteFile(entry.downloadId) }
    }

    override fun openComments(entry: Entry) {
        router.navigateTo(Screens.CommentsScreen(entry = entry))
    }
}
package su.tagir.apps.radiot.ui.podcasts.downloaded

import io.reactivex.rxkotlin.plusAssign
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.BaseListPresenter
import timber.log.Timber

class DownloadedPodcastsPresenter(private val entryRepository: EntryRepository,
                                  private val scheduler: BaseSchedulerProvider,
                                  private val router: Router):
        BaseListPresenter<Entry, DownloadedPodcastsContract.View>(),
        DownloadedPodcastsContract.Presenter {


    override fun doOnAttach(view: DownloadedPodcastsContract.View) {
        observePodcasts()
    }

    private fun observePodcasts() {
        disposables +=
                entryRepository
                        .getDownloadedEntries("podcast")
                        .subscribe({ data ->
                            state = state.copy(data = data)
                        }, { Timber.e(it) })

    }

    override fun loadData(pullToRefresh: Boolean) {

    }

    override fun select(entry: Entry) {
        entryRepository.play(entry)
    }


    override fun remove(entry: Entry) {
        disposables += entryRepository.deleteFile(entry.downloadId)
                .observeOn(scheduler.ui())
                .subscribe({}, { e ->
                    Timber.e(e)
                    view?.showRemoveError(e.message)
                })
    }

    override fun openComments(entry: Entry) {
        router.navigateTo(Screens.CommentsScreen(entry = entry))
    }
}
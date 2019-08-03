package su.tagir.apps.radiot.ui.podcasts.downloaded

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

class DownloadedPodcastsPresenter(private val entryRepository: EntryRepository,
                                  private val scheduler: BaseSchedulerProvider,
                                  private val router: Router):
        BasePresenter<DownloadedPodcastsContract.View>(),
        DownloadedPodcastsContract.Presenter {

    private var state = ViewState<List<Entry>>(status = Status.SUCCESS)
        set(value) {
            field = value
            view?.updateState(value)
        }

    override fun doOnAttach(view: DownloadedPodcastsContract.View) {
        observePodcasts()
        observeClickEvents(view)
    }

    private fun observePodcasts() {
        disposables +=
                entryRepository
                        .getDownloadedEntries("podcast")
                        .subscribe({ data ->
                            state = state.copy(data = data)
                        }, { Timber.e(it) })

    }


    private fun observeClickEvents(v: DownloadedPodcastsContract.View) {
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

        disposables += v.commentClickRequests()
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe { router.navigateTo(Screens.CommentsScreen(entry = it)) }

    }
}
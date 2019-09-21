package su.tagir.apps.radiot.ui.news

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

class NewsPresenter(private val entryRepository: EntryRepository,
                    private val scheduler: BaseSchedulerProvider,
                    private val router: Router): BaseListPresenter<Entry, NewsContract.View>(), NewsContract.Presenter {


    private var loadDisposable: Disposable? = null



    override fun doOnAttach(view: NewsContract.View) {
        super.doOnAttach(view)
        observeNews()
        loadData(false)
    }

    override fun observeNews() {
        disposables += entryRepository
                .getEntries("news", "info")
                .subscribe({
                   state = state.copy(data = it)
                }, { Timber.e(it) })
    }

    override fun loadData(pullToRefresh: Boolean) {
        loadDisposable?.dispose()
        loadDisposable = entryRepository.refreshNews()
                .observeOn(scheduler.ui())
                .doOnSubscribe { state = if (pullToRefresh) state.copy(status = Status.REFRESHING) else state.copy(status = Status.LOADING) }
                .subscribe({ state = state.copy(status = Status.SUCCESS) },
                        {
                            Timber.e(it)
                            state = state.copy(status = Status.ERROR)
                        })

        disposables += loadDisposable!!
    }

    override fun select(entry: Entry) {
        router.navigateTo(Screens.ContentScreen(entry.title, entry.url))
    }

    override fun openComments(entry: Entry) {
        router.navigateTo(Screens.CommentsScreen(entry))
    }

}
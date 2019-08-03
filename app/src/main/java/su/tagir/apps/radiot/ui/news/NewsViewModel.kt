package su.tagir.apps.radiot.ui.news

import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.Status
import su.tagir.apps.radiot.ui.mvp.ViewState
import su.tagir.apps.radiot.ui.viewmodel.ListViewModel
import timber.log.Timber
import javax.inject.Inject

class NewsViewModel
@Inject constructor(private val entryRepository: EntryRepository,
                    schedulerProvider: BaseSchedulerProvider,
                    private val router: Router) : ListViewModel<Entry>(schedulerProvider) {

    private var loadDisposable: Disposable? = null

    init {
        disposable += entryRepository
                .getEntries("news", "info")
                .subscribe({
                    val newState = state.value?.copy(data = it)
                    state.value = newState
                }, { Timber.e(it) })

        loadData()
    }


    override fun loadData() {
        loadDisposable?.dispose()
        loadDisposable = entryRepository.refreshNews()
                .observeOn(scheduler.ui())
                .doOnSubscribe { state.value = if (state.value == null) ViewState(Status.LOADING) else state.value?.copy(Status.LOADING) }
                .subscribe({ state.value = state.value?.copy(status = Status.SUCCESS) },
                        {
                            Timber.e(it)
                            state.value = state.value?.copy(status = Status.ERROR)
                        })

        disposable += loadDisposable!!

    }


    override fun requestUpdates() {
        loadDisposable?.dispose()
        loadDisposable = entryRepository.refreshNews()
                .observeOn(scheduler.ui())
                .doOnSubscribe { state.value = state.value?.copy(status = Status.REFRESHING) }
                .subscribe({ state.value = state.value?.copy(status = Status.SUCCESS) },
                        {
                            Timber.e(it)
                            state.value = state.value?.copy(status = Status.ERROR)
                        })
        disposable += loadDisposable!!
    }

    fun onEntryClick(entry: Entry) {
        router.navigateTo(Screens.ContentScreen(entry.url))
    }

}
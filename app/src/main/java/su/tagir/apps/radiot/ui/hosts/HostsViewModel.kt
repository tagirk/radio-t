package su.tagir.apps.radiot.ui.hosts

import android.arch.paging.RxPagedListBuilder
import io.reactivex.BackpressureStrategy
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Host
import su.tagir.apps.radiot.model.repository.HostRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.viewmodel.ListViewModel
import su.tagir.apps.radiot.ui.viewmodel.State
import su.tagir.apps.radiot.ui.viewmodel.Status
import timber.log.Timber
import javax.inject.Inject

class HostsViewModel
@Inject constructor(private val hostRepository: HostRepository,
                    private val router: Router,
                    schedulerProvider: BaseSchedulerProvider) : ListViewModel<Host>(schedulerProvider) {

    private var loadDisposable: Disposable? = null

    init {
        disposable += RxPagedListBuilder(hostRepository.getHosts(), 50)
                .buildFlowable(BackpressureStrategy.LATEST)
                .subscribe({
                    val newState = state.value?.copy(data = it)
                    state.value = newState
                }, { Timber.e(it) })

        loadData()

    }

    override fun loadData() {
        loadDisposable?.dispose()
        loadDisposable = hostRepository.refreshHosts()
                .observeOn(scheduler.ui())
                .doOnSubscribe { state.value = if (state.value == null) State(Status.LOADING) else state.value?.copy(Status.LOADING) }
                .subscribe({ state.value = state.value?.copy(status = Status.SUCCESS) }, {
                    Timber.e(it)
                    state.value = state.value?.copy(status = Status.ERROR)
                })

        disposable += loadDisposable!!

    }

    override fun requestUpdates() {
        loadDisposable?.dispose()
        loadDisposable = hostRepository
                .refreshHosts()
                .observeOn(scheduler.ui())
                .doOnSubscribe { state.value = state.value?.copy(status = Status.REFRESHING) }
                .subscribe({ state.value = state.value?.copy(status = Status.SUCCESS) }, {
                    Timber.e(it)
                    state.value = state.value?.copy(status = Status.ERROR)
                })
        disposable += loadDisposable!!
    }

    fun openSocialNet(url: String) {
        router.navigateTo(Screens.RESOLVE_ACTIVITY, url)
    }
}
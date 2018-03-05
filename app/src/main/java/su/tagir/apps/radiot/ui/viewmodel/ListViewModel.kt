package su.tagir.apps.radiot.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PagedList
import android.support.annotation.VisibleForTesting
import io.reactivex.disposables.CompositeDisposable
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider

abstract class ListViewModel<T>(scheduler: BaseSchedulerProvider) : BaseViewModel(scheduler) {

    val state = MutableLiveData<ViewModelState?>()

    private val compositeDisposable = CompositeDisposable()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var firstLaunch = true

    abstract fun getData(): LiveData<PagedList<T>>

    fun loadData() {
        if (firstLaunch) {
            update(false)
        }
        firstLaunch = false
    }

    fun update(refresh: Boolean) {
        state.postValue(if (refresh) ViewModelState.REFRESHING else ViewModelState.LOADING)
        requestUpdates()
    }

    protected abstract fun requestUpdates()

}
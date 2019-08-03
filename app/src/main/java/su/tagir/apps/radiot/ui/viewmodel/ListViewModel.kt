package su.tagir.apps.radiot.ui.viewmodel

import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.ViewState

abstract class ListViewModel<T>(scheduler: BaseSchedulerProvider) : BaseViewModel(scheduler) {

    protected val state = MutableLiveData<ViewState<List<T>>>()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var firstLaunch = true

    @MainThread
    abstract fun loadData()

    @MainThread
    open fun loadMore() {
    }

    @MainThread
    fun update() {
        requestUpdates()
    }

    @MainThread
    protected open fun requestUpdates() {
    }

    @MainThread
    fun state(): LiveData<ViewState<List<T>>> = state
}
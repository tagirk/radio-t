package su.tagir.apps.radiot.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.support.annotation.MainThread
import android.support.annotation.VisibleForTesting
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider

abstract class ListViewModel<T>(scheduler: BaseSchedulerProvider) : BaseViewModel(scheduler) {

    protected val state = MutableLiveData<State<List<T>>>()

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
    fun state(): LiveData<State<List<T>>> = state
}
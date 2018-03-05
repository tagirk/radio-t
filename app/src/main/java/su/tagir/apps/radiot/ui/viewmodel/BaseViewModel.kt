package su.tagir.apps.radiot.ui.viewmodel

import android.arch.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider


abstract class BaseViewModel(protected val scheduler: BaseSchedulerProvider): ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    override fun onCleared() {
        compositeDisposable.clear()
    }

    protected fun addDisposable(disposable: Disposable){
        compositeDisposable.add(disposable)
    }

}
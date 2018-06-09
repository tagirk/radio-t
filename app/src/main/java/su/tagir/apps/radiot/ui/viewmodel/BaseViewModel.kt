package su.tagir.apps.radiot.ui.viewmodel

import android.arch.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider


abstract class BaseViewModel(protected val scheduler: BaseSchedulerProvider): ViewModel() {

    protected val disposable = CompositeDisposable()

    override fun onCleared() {
        disposable.clear()
    }

    protected fun addDisposable(disposable: Disposable){
        this.disposable.add(disposable)
    }

}
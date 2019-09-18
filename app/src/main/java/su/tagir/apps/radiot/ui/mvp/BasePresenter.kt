package su.tagir.apps.radiot.ui.mvp

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BasePresenter<V: MvpView>: MvpPresenter<V> {

    protected var view: V? = null

    val disposables = CompositeDisposable()

    override fun attachView(view: V) {
        this.view = view
        this.doOnAttach(view)
    }

    override fun detachView() {
        this.doOnDetach()
        dispose()
        this.view = null
    }

    protected open fun doOnAttach(view: V){

    }

    protected open fun doOnDetach(){

    }

    fun addDisposable(disposable: Disposable){
        disposables.add(disposable)
    }

    fun dispose(){
        disposables.clear()
    }
}
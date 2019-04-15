package su.tagir.apps.radiot.ui.mvp

import android.os.Bundle
import io.reactivex.disposables.CompositeDisposable

abstract class BasePresenter<V: MvpView>: MvpPresenter<V> {

    private var view: V? = null

    private val disposables = CompositeDisposable()

    override fun attachView(view: V) {
        this.view = view
        this.doOnAttach(view)
    }

    override fun detachView() {
        this.doOnDetach()
        dispose()
        this.view = null
    }

    override fun saveState(bundle: Bundle) {

    }

    override fun restoreState(bundle: Bundle) {

    }

    protected fun doOnAttach(view: V){

    }

    protected fun doOnDetach(){

    }

    fun addDisposable(disposable: CompositeDisposable){
        disposables.add(disposable)
    }

    fun dispose(){
        disposables.clear()
    }
}
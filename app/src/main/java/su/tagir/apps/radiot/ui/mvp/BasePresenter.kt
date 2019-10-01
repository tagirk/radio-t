package su.tagir.apps.radiot.ui.mvp

import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

abstract class BasePresenter<V: MvpView>(private val dispatcher: CoroutineDispatcher): MvpPresenter<V>, CoroutineScope {

    protected var view: V? = null

    private lateinit var job: Job

    protected open var exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
        view?.showProgress(false)
        view?.showError(throwable)
    }

    override val coroutineContext: CoroutineContext
        get() = job + dispatcher + exceptionHandler

    override fun attachView(view: V) {
        this.view = view
        job = SupervisorJob()
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

    fun dispose(){
        job.cancel()
    }


}
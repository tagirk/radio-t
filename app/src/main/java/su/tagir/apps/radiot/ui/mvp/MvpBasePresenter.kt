package su.tagir.apps.radiot.ui.mvp

import androidx.annotation.UiThread
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext


abstract class MvpBasePresenter<V : MvpView>(protected val dispatcher: CoroutineDispatcher) : MvpPresenter<V>, CoroutineScope {

    private var viewRef: WeakReference<V>? = null
    private var presenterDestroyed = false

    protected val job: Job = SupervisorJob()

    protected open var exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
        ifViewAttached(action = {v ->
                v.showProgress(false)
                v.showError(throwable)
        })
    }

    override val coroutineContext: CoroutineContext
        get() = job + dispatcher + exceptionHandler

    protected fun ifViewAttached(action: (V) -> Unit, exceptionIfViewNotAttached: Boolean = false) {
        val view = viewRef?.get()
        if (view != null) {
            action(view)
        } else check(!exceptionIfViewNotAttached) { "No View attached to Presenter. Presenter destroyed = $presenterDestroyed" }
    }

    @UiThread
    override fun attachView(view: V) {
        viewRef = WeakReference(view)
        presenterDestroyed = false
        this.doOnAttach(view)
    }

    override fun detachView() {
        this.doOnDetach()
        viewRef?.clear()
        viewRef = null
    }

    override fun destroy() {
        job.cancel()
        presenterDestroyed = true
    }

    protected open fun doOnAttach(view: V) {

    }

    protected open fun doOnDetach() {

    }
}
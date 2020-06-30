package su.tagir.apps.radiot.ui.mvp

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import timber.log.Timber

abstract class MvpBaseListPresenter<M, V: MvpListView<M>>(dispatcher: CoroutineDispatcher): MvpBasePresenter<V>(dispatcher), MvpListPresenter<M, V> {

    protected var state = ViewState<List<M>>(status = Status.SUCCESS, data = emptyList())
    set(value) {
        field = value
        ifViewAttached({v -> v.updateState(value)})
    }

    override var exceptionHandler: CoroutineExceptionHandler = CoroutineExceptionHandler{ _, throwable ->
        Timber.e(throwable)
        state = state.copy(status = Status.ERROR, errorMessage = throwable.message)
    }

    override fun doOnAttach(view: V) {
        super.doOnAttach(view)
        loadData(false)
    }

    override fun loadMore(lastIndex: Int) {

    }
}
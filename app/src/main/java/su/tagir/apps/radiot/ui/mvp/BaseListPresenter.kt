package su.tagir.apps.radiot.ui.mvp

abstract class BaseListPresenter<M, V: MvpListView<M>>: BasePresenter<V>(), MvpListPresenter<M, V> {

    protected var state = ViewState<List<M>>(status = Status.SUCCESS, data = emptyList())
    set(value) {
        field = value
        view?.updateState(value)
    }

    override fun doOnAttach(view: V) {
        super.doOnAttach(view)
        loadData(false)
    }

    override fun loadMore(lastIndex: Int) {

    }
}
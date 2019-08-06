package su.tagir.apps.radiot.ui.mvp

abstract class BaseListPresenter<M, V: MvpListView<M>>: BasePresenter<V>(), MvpListPresenter<M, V> {

    override fun loadMore(lastIndex: Int) {

    }

}
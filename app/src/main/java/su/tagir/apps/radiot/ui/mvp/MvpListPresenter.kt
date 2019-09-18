package su.tagir.apps.radiot.ui.mvp

interface MvpListPresenter<M, V: MvpListView<M>>: MvpPresenter<V> {

    fun loadData(pullToRefresh: Boolean)

    fun loadMore(lastIndex: Int)
}
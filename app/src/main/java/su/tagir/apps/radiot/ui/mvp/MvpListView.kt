package su.tagir.apps.radiot.ui.mvp

interface MvpListView<M>: MvpView {

    fun loadData(pullToRefresh: Boolean)

    fun loadMore(lastIndex: Int)

    fun updateState(viewState: ViewState<List<M>>)
}
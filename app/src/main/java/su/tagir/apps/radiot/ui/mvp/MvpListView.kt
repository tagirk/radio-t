package su.tagir.apps.radiot.ui.mvp

interface MvpListView<M>: MvpView {

    fun updateState(viewState: ViewState<List<M>>)
}
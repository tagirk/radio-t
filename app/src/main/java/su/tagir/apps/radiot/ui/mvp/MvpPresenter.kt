package su.tagir.apps.radiot.ui.mvp

interface MvpPresenter<V : MvpView> {

    fun attachView(view: V)
    fun detachView()

}

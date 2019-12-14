package su.tagir.apps.radiot.ui.mvp

interface MvpView{

    fun showError(t: Throwable)
    fun showProgress(show: Boolean)
}

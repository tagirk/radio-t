package su.tagir.apps.radiot.ui.mvp

import android.os.Bundle

interface MvpPresenter<V : MvpView> {

    fun attachView(view: V)
    fun detachView()

    fun saveState(bundle: Bundle)
    fun restoreState(bundle: Bundle)
}

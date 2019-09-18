package su.tagir.apps.radiot.ui.chat

import su.tagir.apps.radiot.ui.mvp.MvpPresenter
import su.tagir.apps.radiot.ui.mvp.MvpView
import su.tagir.apps.radiot.ui.mvp.ViewState

interface AuthContract {

    interface View: MvpView{
        fun auth(url: String)
        fun updateState(state: ViewState<Boolean>)
    }

    interface Presenter: MvpPresenter<View>{
        fun startAuth()
        fun requestToken(redirectString: String)
        fun onBackClick()
    }
}
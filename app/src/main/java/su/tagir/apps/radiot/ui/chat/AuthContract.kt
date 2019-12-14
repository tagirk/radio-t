package su.tagir.apps.radiot.ui.chat

import su.tagir.apps.radiot.ui.mvp.MvpPresenter
import su.tagir.apps.radiot.ui.mvp.MvpView

interface AuthContract {

    interface View: MvpView{
        fun auth(url: String)
        fun clearCookies()
    }

    interface Presenter: MvpPresenter<View>{
        fun startAuth()
        fun requestToken(redirectString: String)
        fun onBackClick()
    }
}
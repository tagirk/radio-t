package su.tagir.apps.radiot.ui.comments

import su.tagir.apps.radiot.ui.mvp.MvpPresenter
import su.tagir.apps.radiot.ui.mvp.MvpView

interface AuthContract {

    interface View: MvpView{

    }

    interface Presenter: MvpPresenter<View>{
        fun signInGoogle()
    }

}
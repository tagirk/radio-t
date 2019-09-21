package su.tagir.apps.radiot.ui

import su.tagir.apps.radiot.ui.mvp.MvpPresenter
import su.tagir.apps.radiot.ui.mvp.MvpView

interface MainContract {

    interface View: MvpView{
        fun showTime(time: String)
    }

    interface Presenter: MvpPresenter<View>{
        fun navigateToPodcasts()
        fun navigateToNews()
        fun navigateToSettings()
        fun navigateToChat()
        fun navigateToPirates()
        fun navigateToAbout()
        fun navigateToCredits()
        fun startTimerToNextShow()
    }
}
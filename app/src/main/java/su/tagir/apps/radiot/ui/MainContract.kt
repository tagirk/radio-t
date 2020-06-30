package su.tagir.apps.radiot.ui

import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.mvp.MvpPresenter
import su.tagir.apps.radiot.ui.mvp.MvpView

interface MainContract {

    interface View: MvpView{
        fun showCurrentPodcast(entry: Entry)
    }

    interface Presenter: MvpPresenter<View>{
        fun navigateToPodcasts()
        fun navigateToNews()
        fun navigateToSettings()
        fun navigateToPirates()
        fun navigateToAbout()
        fun navigateToCredits()
        fun observeCurrentPodcast()
    }
}
package su.tagir.apps.radiot.ui.podcasts

import su.tagir.apps.radiot.ui.mvp.MvpPresenter
import su.tagir.apps.radiot.ui.mvp.MvpView

interface PodcastsTabsContract {

    interface View: MvpView{
        fun showOrHideStream(show: Boolean)
        fun showStreamTime(time: String?)
    }

    interface Presenter: MvpPresenter<View>{
        fun observeCurrentPodcast()
        fun startTimer()
        fun playStream()
    }
}
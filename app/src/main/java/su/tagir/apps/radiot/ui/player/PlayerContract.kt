package su.tagir.apps.radiot.ui.player

import io.reactivex.Observable
import su.tagir.apps.radiot.model.entries.Article
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.TimeLabel
import su.tagir.apps.radiot.ui.mvp.MvpPresenter
import su.tagir.apps.radiot.ui.mvp.MvpView

interface PlayerContract {

    interface View: MvpView{
        fun showCurrentPodcast(entry: Entry)
        fun showTimeLabels(timeLabels: List<TimeLabel>)
        fun requestProgress()
        fun seekTo(seek: Long)
        fun showError(error: String)
        fun showLoading(loading: Boolean)

        fun timeLabelRequests(): Observable<TimeLabel>
        fun onSlide(offset: Float)
    }

    interface Presenter: MvpPresenter<View>{

        fun playStream()
        fun pause()
        fun resume()
        fun onArticleClick(article: Article?)
        fun onChatClick()
        fun openWebPage()
        fun onTitleClick()
        fun setListener(listener: InteractionListener)

        interface InteractionListener{
            fun onExpand()
            fun showCurrent(podcast: Entry?)
        }

    }
}
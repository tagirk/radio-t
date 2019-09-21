package su.tagir.apps.radiot.ui.player

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
        fun onSlide(offset: Float)
    }

    interface Presenter: MvpPresenter<View>{

        fun playStream()
        fun pause()
        fun resume()
        fun onArticleClick(article: Article?)
        fun showChat()
        fun openWebPage()
        fun onTitleClick()
        fun setListener(listener: InteractionListener)
        fun seekTo(timeLabel: TimeLabel)

        interface InteractionListener{
            fun onExpand()
            fun showCurrent(podcast: Entry?)
        }

    }
}
package su.tagir.apps.radiot.ui.podcasts

import io.reactivex.Observable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.mvp.MvpListView
import su.tagir.apps.radiot.ui.mvp.MvpPresenter

interface PodcastsContract {

    interface View: MvpListView<Entry>{
        fun showDownloadError(error: String)
        fun download()

        fun entryClickRequests(): Observable<Entry>
        fun downloadClickRequests(): Observable<Entry>
        fun removeClickRequests(): Observable<Entry>
        fun commentClickRequests(): Observable<Entry>
    }

    interface Presenter: MvpPresenter<View>{
        fun update()
        fun download()
    }
}
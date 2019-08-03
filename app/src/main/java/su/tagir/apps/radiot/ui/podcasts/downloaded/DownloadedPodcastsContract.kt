package su.tagir.apps.radiot.ui.podcasts.downloaded

import io.reactivex.Observable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.mvp.MvpListView
import su.tagir.apps.radiot.ui.mvp.MvpPresenter

interface DownloadedPodcastsContract{

    interface View: MvpListView<Entry> {
        fun showRemoveError(error: String)
        fun entryClickRequests(): Observable<Entry>
        fun removeClickRequests(): Observable<Entry>
        fun commentClickRequests(): Observable<Entry>

    }

    interface Presenter: MvpPresenter<View>
}

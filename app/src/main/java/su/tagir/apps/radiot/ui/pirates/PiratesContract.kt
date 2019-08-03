package su.tagir.apps.radiot.ui.pirates

import io.reactivex.Observable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.mvp.MvpListView
import su.tagir.apps.radiot.ui.mvp.MvpPresenter

interface PiratesContract{

    interface View: MvpListView<Entry>{
        fun download()
        fun showDownloadError(error: String)

        fun entryClickRequests(): Observable<Entry>
        fun downloadClickRequests(): Observable<Entry>
        fun removeClickRequests(): Observable<Entry>
    }

    interface Presenter: MvpPresenter<View>{
        fun update()
        fun download()
    }

}



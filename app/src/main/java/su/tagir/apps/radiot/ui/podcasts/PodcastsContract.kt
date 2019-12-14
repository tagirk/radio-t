package su.tagir.apps.radiot.ui.podcasts

import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.mvp.MvpListPresenter
import su.tagir.apps.radiot.ui.mvp.MvpListView

interface PodcastsContract {

    interface View: MvpListView<Entry>{
        fun showDownloadError(error: String?)
    }

    interface Presenter: MvpListPresenter<Entry, View>{
        fun openComments(entry: Entry)
        fun download(entry: Entry)
        fun select(entry: Entry)
        fun remove(entry: Entry)

    }
}
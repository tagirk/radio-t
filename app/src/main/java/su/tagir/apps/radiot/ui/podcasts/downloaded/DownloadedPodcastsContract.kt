package su.tagir.apps.radiot.ui.podcasts.downloaded

import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.mvp.MvpListPresenter
import su.tagir.apps.radiot.ui.mvp.MvpListView

interface DownloadedPodcastsContract{

    interface View: MvpListView<Entry> {
        fun showRemoveError(error: String?)

    }

    interface Presenter: MvpListPresenter<Entry, View>{
        fun openComments(entry: Entry)
        fun select(entry: Entry)
        fun remove(entry: Entry)
    }
}

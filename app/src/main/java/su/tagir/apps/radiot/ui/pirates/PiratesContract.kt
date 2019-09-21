package su.tagir.apps.radiot.ui.pirates

import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.mvp.MvpListPresenter
import su.tagir.apps.radiot.ui.mvp.MvpListView

interface PiratesContract{

    interface View: MvpListView<Entry>{
        fun download()
        fun showDownloadError(error: String?)
    }

    interface Presenter: MvpListPresenter<Entry, View>{
        fun download(entry: Entry)
        fun select(entry: Entry)
        fun remove(entry: Entry)
    }

}



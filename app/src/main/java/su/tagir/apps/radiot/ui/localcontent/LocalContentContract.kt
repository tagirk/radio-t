package su.tagir.apps.radiot.ui.localcontent

import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.mvp.MvpPresenter
import su.tagir.apps.radiot.ui.mvp.MvpView

interface LocalContentContract {

    interface View: MvpView{
        fun showContent(entry: Entry)
    }

    interface Presenter: MvpPresenter<View>{
        fun loadContent(id: String)
        fun openInBrowser()
        fun exit()
    }
}
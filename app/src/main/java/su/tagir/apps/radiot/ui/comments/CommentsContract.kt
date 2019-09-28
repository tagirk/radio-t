package su.tagir.apps.radiot.ui.comments

import su.tagir.apps.radiot.model.entries.Node
import su.tagir.apps.radiot.ui.mvp.MvpListPresenter
import su.tagir.apps.radiot.ui.mvp.MvpListView

interface CommentsContract {

    interface View: MvpListView<Node>{

    }

    interface Presenter: MvpListPresenter<Node, View>{
        fun showReplies(position: Int, node: Node)
        fun hideReplies(position: Int, node: Node)
        fun openUrl(url: String)
    }


}
package su.tagir.apps.radiot.ui.chat

import su.tagir.apps.radiot.model.entries.MessageFull
import su.tagir.apps.radiot.ui.mvp.MvpListPresenter
import su.tagir.apps.radiot.ui.mvp.MvpListView
import su.tagir.apps.radiot.ui.mvp.ViewState

interface ChatContract {

    interface View: MvpListView<MessageFull>{
        fun showSendState(state: ViewState<Void>)
        fun logout()
    }

    interface Presenter: MvpListPresenter<MessageFull, View>{
        fun observeMessages()
        fun sendMessage(message: String)
        fun onBackClicked()
        fun onSignInClick()
        fun signOut()
        fun onWebVersionClick()
        fun onMentionClick(mention: String?)
        fun onUrlClick(url: String?)
        fun subscribeStream()
    }
}
package su.tagir.apps.radiot.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.OnClick
import butterknife.OnEditorAction
import butterknife.OnTextChanged
import com.google.android.material.floatingactionbutton.FloatingActionButton
import su.tagir.apps.radiot.GlideApp
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.MessageFull
import su.tagir.apps.radiot.ui.common.BackClickHandler
import su.tagir.apps.radiot.ui.common.PagedListFragment
import su.tagir.apps.radiot.ui.mvp.ViewState
import su.tagir.apps.radiot.utils.visibleGone
import su.tagir.apps.radiot.utils.visibleInvisible
import javax.inject.Inject

class ChatFragment : PagedListFragment<MessageFull>(), Injectable, MessagesAdapter.Callback, BackClickHandler {

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.btn_downward)
    lateinit var btnDownward: FloatingActionButton

    @BindView(R.id.send)
    lateinit var btnSend: ImageButton

    @BindView(R.id.send_progress)
    lateinit var sendProgress: ProgressBar

    @BindView(R.id.message)
    lateinit var message: EditText

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var chatViewModel: ChatViewModel

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        chatViewModel = listViewModel as ChatViewModel
        toolbar.setNavigationOnClickListener { onBackClick() }
        toolbar.inflateMenu(R.menu.menu_chat)
        toolbar.setOnMenuItemClickListener { item ->
            when(item?.itemId){
                R.id.exit -> chatViewModel.signOut()
            }
            false
        }
        initMessages()
    }

    override fun createViewModel() = ViewModelProviders.of(activity!!, viewModelFactory).get(ChatViewModel::class.java)

    override fun createAdapter() = MessagesAdapter(GlideApp.with(this), this)

    override fun onResume() {
        super.onResume()
        chatViewModel.loadData()
    }

    override fun onBackClick() {
        chatViewModel.onBackClicked()
    }

    @OnClick(R.id.btn_downward)
    fun scrollToBottom() {
        list.scrollToPosition(0)
    }

    @OnClick(R.id.send)
    fun sendMessage() {
        chatViewModel.sendMessage(message.text.toString())
        dismissKeyboard(view?.applicationWindowToken)
    }

    @OnTextChanged(R.id.message)
    fun onMessageChanged(message: CharSequence) {
        btnSend.visibleInvisible(message.isNotBlank())
    }

    @OnEditorAction(R.id.message)
    fun onAction(): Boolean {
        sendMessage()
        return false
    }

    override fun onMentionClick(mention: String?) {
        chatViewModel.onMentionClick(mention)
    }

    override fun onUrlClick(url: String?) {
        chatViewModel.onUrlClick(url)
    }

    @SuppressLint("SetTextI18n")
    override fun onUserNameClick(mention: String?) {
        message.setText(if (message.text.isNullOrEmpty()) mention else "${message.text} $mention")
        message.setSelection(message.text.lastIndex)
    }

    private fun initMessages() {
        list.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, true)
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val pos = (list.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pos > 0) {
                    btnDownward.show()
                } else {
                    btnDownward.hide()
                }

            }
        })
        refreshLayout.isEnabled = false

        chatViewModel
                .messageSendState
                .observe(getViewLifecycleOwner()!!, Observer { t ->
                    sendProgress.visibleInvisible(t?.loading == true)
                    btnSend.visibleInvisible(t?.loading != true)
                    val error = t?.getErrorIfNotHandled()
                    if (error != null) {
                        showToast(error)
                    }
                    if(t?.completed==true){
                        message.setText("")
                    }
                })
    }

    override fun showHideViews(viewState: ViewState<List<MessageFull>>?) {
        super.showHideViews(viewState)
        loadMoreProgress.visibleGone(false)
    }

    private fun dismissKeyboard(windowToken: IBinder?) {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)

    }
}
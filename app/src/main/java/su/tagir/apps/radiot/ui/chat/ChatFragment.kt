package su.tagir.apps.radiot.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.entries.MessageFull
import su.tagir.apps.radiot.ui.common.BackClickHandler
import su.tagir.apps.radiot.ui.mvp.BaseMvpListFragment
import su.tagir.apps.radiot.ui.mvp.ViewState
import su.tagir.apps.radiot.utils.BetterLinkMovementMethod
import su.tagir.apps.radiot.utils.visibleGone
import su.tagir.apps.radiot.utils.visibleInvisible


class ChatFragment : BaseMvpListFragment<MessageFull, ChatContract.View, ChatContract.Presenter>(), ChatContract.View,
        MessagesAdapter.Callback,
        BackClickHandler {

    private lateinit var toolbar: Toolbar

    private lateinit var btnDownward: FloatingActionButton

    private lateinit var btnSend: ImageButton

    private lateinit var sendProgress: ProgressBar

    private lateinit var message: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_chat, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = view.findViewById(R.id.toolbar)
        btnDownward = view.findViewById(R.id.btn_downward)
        btnSend = view.findViewById(R.id.send)
        sendProgress = view.findViewById(R.id.send_progress)
        message = view.findViewById(R.id.message)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        toolbar.setNavigationOnClickListener { onBackClick() }
        toolbar.inflateMenu(R.menu.menu_chat)
        toolbar.setOnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.exit -> presenter.signOut()
            }
            false
        }
        initMessages()

        btnDownward.setOnClickListener { list.scrollToPosition(0) }
        btnSend.setOnClickListener { sendMessage() }
        message.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            false
        }

        message.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                btnSend.visibleInvisible(!p0.isNullOrBlank())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        })
    }

    override fun createAdapter() = MessagesAdapter(this, BetterLinkMovementMethod
            .linkify(Linkify.WEB_URLS, activity)
            .setOnLinkClickListener { _, url ->
                presenter.onUrlClick(url)
                true
            })

    override fun onBackClick() {
        presenter.onBackClicked()
    }

    private fun sendMessage() {
        presenter.sendMessage(message.text.toString())
        dismissKeyboard(view?.applicationWindowToken)
    }

    override fun createPresenter(): ChatContract.Presenter {
        val appComponent = (activity!!.application as App).appComponent
        return ChatPresenter(appComponent.chatRepository, appComponent.router)
    }

    override fun showSendState(state: ViewState<Void>) {
        sendProgress.visibleInvisible(state.loading)
        btnSend.visibleInvisible(state.loading)
        val error = state.getErrorIfNotHandled()
        if (error != null) {
            showToast(error)
        }
        if (state.completed) {
            message.setText("")
        }
    }

    override fun onMentionClick(mention: String?) {
        presenter.onMentionClick(mention)
    }

    @SuppressLint("SetTextI18n")
    override fun onUserNameClick(mention: String?) {
        message.setText(if (message.text.isNullOrEmpty()) mention else "${message.text} $mention")
        message.setSelection(message.text.lastIndex)
    }

    override fun showHideViews(viewState: ViewState<List<MessageFull>>) {
        super.showHideViews(viewState)
        loadMoreProgress.visibleGone(false)
    }

    override fun logout() {

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
    }

    private fun dismissKeyboard(windowToken: IBinder?) {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)

    }
}
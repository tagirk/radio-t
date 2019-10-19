package su.tagir.apps.radiot.ui.chat

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.MessageFull
import su.tagir.apps.radiot.model.repository.ChatRepository
import su.tagir.apps.radiot.ui.MainDispatcher
import su.tagir.apps.radiot.ui.mvp.BaseListPresenter
import su.tagir.apps.radiot.ui.mvp.Status
import su.tagir.apps.radiot.ui.mvp.ViewState
import timber.log.Timber

class ChatPresenter(
        private val chatRepository: ChatRepository,
        private val router: Router,
        dispatcher: CoroutineDispatcher = MainDispatcher()) : BaseListPresenter<MessageFull, ChatContract.View>(dispatcher), ChatContract.Presenter {

    private var loadJob: Job? = null

    private var messageSendState: ViewState<Void> = ViewState(Status.SUCCESS)
        set(value) {
            field = value
            view?.showSendState(value)
        }

    override fun doOnAttach(view: ChatContract.View) {
        if(!chatRepository.isSignedIn){
            router.newRootScreen(Screens.ChatAuthScreen)
            return
        }

        chatRepository.subcribeAuthNeeded(object : AuthListener {
            override fun needAuth() {
                router.newRootScreen(Screens.ChatAuthScreen)
            }
        })
        observeMessages()
        loadData(false)
        subscribeStream()
    }

    override fun doOnDetach() {
        loadJob?.cancel()
    }

    override fun observeMessages() {
        launch {
            chatRepository.getMessages()
                    .collect{list ->
                        Timber.d("messages: $list")
                        state = state.copy(data = list)}
        }
    }

    override fun loadData(pullToRefresh: Boolean) {
        loadJob?.cancel()
        loadJob = launch {
            state = state.copy(Status.LOADING)
            chatRepository.loadMessages()
            state = state.copy(status = Status.SUCCESS)
        }
    }

    override fun loadMore(lastIndex: Int) {
        if (state.loading || state.loadingMore) {
            return
        }
        launch {
            state = state.copy(status = Status.LOADING_MORE)
            chatRepository.loadMessages(state.data?.last()?.message?.id)
            state = state.copy(status = Status.SUCCESS)
        }
    }

    override fun sendMessage(message: String) {
        launch {
            messageSendState = ViewState(Status.LOADING)
            chatRepository.sendMessage(message.trim())
            messageSendState = ViewState(Status.SUCCESS)
        }
    }

    override fun onBackClicked() {
        router.exit()
    }

    override fun onSignInClick() {
        router.navigateTo(Screens.ChatAuthScreen)
    }

    override fun signOut() {
        chatRepository.removeAuthData()
        router.newRootScreen(Screens.ChatAuthScreen)
    }

    override fun onWebVersionClick() {
        router.navigateTo(Screens.WebScreen("https://gitter.im/radio-t/chat"))
    }

    override fun onMentionClick(mention: String?) {
        router.navigateTo(Screens.WebScreen("https://github.com/$mention"))
    }

    override fun onUrlClick(url: String?) {
        url?.let {
            router.navigateTo(Screens.WebScreen(url))
        }
    }

    override fun subscribeStream() {
//        disposables += chatRepository
//                .getMessageStream()
//                .subscribeOn(scheduler.io())
//                .observeOn(scheduler.ui())
//                .subscribe({}, { Timber.e(it) })
//
//        disposables += chatRepository
//                .getEventsStream()
//                .subscribeOn(scheduler.io())
//                .observeOn(scheduler.io())
//                .subscribe({}, { Timber.e(it) })
    }
}
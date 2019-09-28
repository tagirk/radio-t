package su.tagir.apps.radiot.ui.chat

import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import retrofit2.HttpException
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.MessageFull
import su.tagir.apps.radiot.model.repository.ChatRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.BaseListPresenter
import su.tagir.apps.radiot.ui.mvp.Status
import su.tagir.apps.radiot.ui.mvp.ViewState
import timber.log.Timber

class ChatPresenter(
        private val chatRepository: ChatRepository,
        private val router: Router,
        private val scheduler: BaseSchedulerProvider) : BaseListPresenter<MessageFull, ChatContract.View>(), ChatContract.Presenter {

    private var loadDisposable: Disposable? = null

    private var messageSendState: ViewState<Void> = ViewState(Status.SUCCESS)
        set(value) {
            field = value
            view?.showSendState(value)
        }

    override fun doOnAttach(view: ChatContract.View) {
        chatRepository.subcribeAuthNeeded(object : AuthListener {
            override fun needAuth() {
                router.newRootScreen(Screens.ChatAuthScreen)
            }
        })
        observeMessages()
        loadData(false)
        subscribeStream()
    }

    override fun observeMessages() {
        disposables += chatRepository.getMessages()
                .subscribe({list ->
                    state = state.copy(data = list)
                }, { Timber.e(it) })

    }

    override fun loadData(pullToRefresh: Boolean) {
        loadDisposable?.dispose()
        loadDisposable = chatRepository
                .loadMessages()
                .observeOn(scheduler.ui())
                .doOnSubscribe { state = state.copy(Status.LOADING) }
                .subscribe({ state = state.copy(status = Status.SUCCESS) }, {
                    Timber.e(it)
                    state = state.copy(status = Status.ERROR)
                })
        disposables += loadDisposable!!
    }

    override fun loadMore(lastIndex: Int) {
        if (state.loading || state.loadingMore) {
            return
        }

        disposables += chatRepository.loadMessages(state.data?.last()?.message?.id)
                .observeOn(scheduler.ui())
                .doOnSubscribe { state = state.copy(status = Status.LOADING_MORE) }
                .subscribe({ state = state.copy(status = Status.SUCCESS) },
                        {
                            Timber.e(it)
                            state = state.copy(status = Status.ERROR)
                        })
        disposables += loadDisposable!!
    }

    override fun sendMessage(message: String) {
        disposables += chatRepository.sendMessage(message.trim())
                .observeOn(scheduler.ui())
                .doOnSubscribe { messageSendState = ViewState(Status.LOADING) }
                .subscribe({
                    messageSendState = ViewState(Status.SUCCESS)
                }, {
                    Timber.e(it)
                    messageSendState = ViewState(Status.ERROR, errorMessage = it.message)
                    if (it is HttpException && it.code() == 401) {
                        signOut()
                    }
                })

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
        disposables += chatRepository
                .getMessageStream()
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribe({}, { Timber.e(it) })

        disposables += chatRepository
                .getEventsStream()
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.io())
                .subscribe({}, { Timber.e(it) })
    }
}
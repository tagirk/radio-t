package su.tagir.apps.radiot.ui.chat

import android.arch.lifecycle.MutableLiveData
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import retrofit2.HttpException
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.MessageFull
import su.tagir.apps.radiot.model.repository.ChatRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.viewmodel.ListViewModel
import su.tagir.apps.radiot.ui.viewmodel.State
import su.tagir.apps.radiot.ui.viewmodel.Status
import timber.log.Timber
import javax.inject.Inject

class ChatViewModel @Inject constructor(
        private val chatRepository: ChatRepository,
        private val router: Router,
        scheduler: BaseSchedulerProvider) : ListViewModel<MessageFull>(scheduler) {

    val messageSendState = MutableLiveData<State<Void>>()

    private var loadDisposable: Disposable? = null

    val isSignedIn: Boolean
        get() = chatRepository.isSignedIn

    init {
        disposable += chatRepository.getMessages()
                .subscribe({
                    val newState = state.value?.copy(data = it)
                    state.value = newState
                }, { Timber.e(it) })

    }


    override fun loadData() {
        loadDisposable?.dispose()
        loadDisposable = chatRepository
                .loadMessages()
                .observeOn(scheduler.ui())
                .doOnSubscribe { state.value = if (state.value == null) State(Status.LOADING) else state.value?.copy(Status.LOADING) }
                .subscribe({ state.value = state.value?.copy(status = Status.SUCCESS) }, {
                    Timber.e(it)
                    state.value = state.value?.copy(status = Status.ERROR)
                })
        disposable += loadDisposable!!
        subscribeStream()
    }

    override fun loadMore() {
        Timber.d("loadMore1")
        if (loadDisposable != null && !loadDisposable!!.isDisposed) {
            return
        }
        Timber.d("loadMore2")
        loadDisposable = chatRepository.loadMessages(state.value?.data?.last()?.message?.id)
                .observeOn(scheduler.ui())
                .doOnSubscribe { state.value = state.value?.copy(status = Status.LOADING_MORE) }
                .subscribe({ state.value = state.value?.copy(status = Status.SUCCESS) }, {
                    Timber.e(it)
                    state.value = state.value?.copy(status = Status.ERROR)
                })
        disposable += loadDisposable!!
    }

    fun sendMessage(message: String) {
        addDisposable(chatRepository.sendMessage(message.trim())
                .observeOn(scheduler.ui())
                .doOnSubscribe { messageSendState.value = State(Status.LOADING) }
                .subscribe({
                    messageSendState.value = State(Status.SUCCESS)
                }, {
                    Timber.e(it)
                    messageSendState.value = State(Status.ERROR, errorMessage = it.message)
                    if (it is HttpException && it.code() == 401) {
                        signOut()
                    }
                }))

    }

    fun onBackClicked() {
        router.exit()
    }

    fun onSignInClick() {
        router.navigateTo(Screens.CHAT_AUTH_SCREEN)
    }

    fun signOut() {
        chatRepository.removeAuthData()
        router.exit()
    }

    fun onWebVersionClick() {
        router.navigateTo(Screens.WEB_SCREEN, "https://gitter.im/radio-t/chat")
    }

    fun onMentionClick(mention: String?) {
        router.navigateTo(Screens.WEB_SCREEN, "https://github.com/$mention")
    }

    fun onUrlClick(url: String?) {
        router.navigateTo(Screens.WEB_SCREEN, url)
    }

    private fun subscribeStream() {
        disposable+=chatRepository
                .getMessageStream()
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribe({}, { Timber.e(it) })

       disposable+=chatRepository
                .getEventsStream()
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.io())
                .subscribe({}, { Timber.e(it) })
    }
}
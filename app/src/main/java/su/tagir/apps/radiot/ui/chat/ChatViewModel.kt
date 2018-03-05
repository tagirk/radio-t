package su.tagir.apps.radiot.ui.chat

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import io.reactivex.disposables.Disposable
import retrofit2.HttpException
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.MessageFull
import su.tagir.apps.radiot.model.repository.ChatRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.common.SingleLiveEvent
import su.tagir.apps.radiot.ui.viewmodel.ListViewModel
import su.tagir.apps.radiot.ui.viewmodel.ViewModelState
import timber.log.Timber
import javax.inject.Inject

class ChatViewModel @Inject constructor(
        private val chatRepository: ChatRepository,
        private val router: Router,
        scheduler: BaseSchedulerProvider) : ListViewModel<MessageFull>(scheduler) {

    val messageSendState = MutableLiveData<ViewModelState>()
    val messageSendSuccess = SingleLiveEvent<Void>()

    private var loadDisposable: Disposable? = null

    val isSignedIn: Boolean
        get() = chatRepository.isSignedIn

    override fun getData() = LivePagedListBuilder(chatRepository.getMessages(), 50)
            .setBoundaryCallback(boundaryCallback)
            .build()


    override fun requestUpdates() {
        loadDisposable?.dispose()
        loadDisposable = chatRepository
                .updateMessages()
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribe({
                    state.value = ViewModelState.COMPLETE
                }, {
                    Timber.e(it)
                    state.value = ViewModelState.error(it.message)
                    if (it is HttpException && it.code() == 401) {
                        signOut()
                    }
                })
        addDisposable(loadDisposable!!)
        subscribeStream()
    }

    fun sendMessage(message: String) {
        addDisposable(chatRepository.sendMessage(message.trim())
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .doOnSubscribe { messageSendState.value = ViewModelState.LOADING }
                .subscribe({
                    messageSendState.value = ViewModelState.COMPLETE
                    messageSendSuccess.call()
                }, {
                    Timber.e(it)
                    messageSendState.value = ViewModelState.error(it.message)
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
        addDisposable(chatRepository
                .getMessageStream()
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribe({}, { Timber.e(it) }))

        addDisposable(chatRepository
                .getEventsStream()
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.io())
                .subscribe({}, { Timber.e(it) }))
    }

    private val boundaryCallback = object : PagedList.BoundaryCallback<MessageFull>() {

        override fun onItemAtEndLoaded(itemAtEnd: MessageFull) {
            if (loadDisposable != null && !loadDisposable!!.isDisposed) {
                return
            }
            val id = itemAtEnd.message?.id
            state.value = ViewModelState.LOADING_MORE
            loadDisposable = chatRepository
                    .loadMessages(id)
                    .subscribeOn(scheduler.io())
                    .observeOn(scheduler.ui())
                    .subscribe({
                        state.value = ViewModelState.COMPLETE
                    }, {
                        Timber.e(it)
                        state.value = ViewModelState.error(it.message)
                        if (it is HttpException && it.code() == 401) {
                            signOut()
                        }
                    })

            addDisposable(loadDisposable!!)
        }
    }
}
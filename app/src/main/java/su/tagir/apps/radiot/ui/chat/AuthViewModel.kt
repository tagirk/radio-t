package su.tagir.apps.radiot.ui.chat

import android.arch.lifecycle.MutableLiveData
import io.reactivex.Single
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.repository.ChatRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.common.SingleLiveEvent
import su.tagir.apps.radiot.ui.viewmodel.BaseViewModel
import su.tagir.apps.radiot.ui.viewmodel.ViewModelState
import timber.log.Timber
import java.net.URI
import javax.inject.Inject

class AuthViewModel @Inject constructor(scheduler: BaseSchedulerProvider,
                                        private val chatRepository: ChatRepository,
                                        private val router: Router) : BaseViewModel(scheduler) {


    val authEvent = SingleLiveEvent<String>()
    val message = SingleLiveEvent<String?>()
    val state = MutableLiveData<ViewModelState>()

    var authParams: AuthParams? = null

    fun startAuth() {
        authEvent.value = "https://gitter.im/login/oauth/authorize?client_id=${authParams?.appId}&" +
                "response_type=${authParams?.responseType}&redirect_uri=${authParams?.redirectUrl}"
    }

    fun redirect(redirectString: String): Boolean {
        return if (authParams != null && redirectString.startsWith(authParams!!.redirectUrl)) {
            requestToken(redirectString)
            true
        } else {
            false
        }
    }

    fun onBackClicked() {
        router.exit()
    }

    private fun requestToken(redirectString: String?) {
        state.value = ViewModelState.LOADING
        val disposable =
                Single.fromCallable { getQueryParameterFromUri(redirectString, "code") }
                        .flatMap { code ->
                            chatRepository
                                    .getOAuthToken(
                                            authParams?.appId,
                                            authParams?.clientId,
                                            code,
                                            authParams?.redirectUrl)
                        }
                        .subscribeOn(scheduler.io())
                        .observeOn(scheduler.ui())
                        .subscribe({
                            router.newRootScreen(Screens.CHAT_SCREEN)
                            state.value = ViewModelState.COMPLETE
                        },
                                {
                                    Timber.e(it)
                                    message.value = it.message
                                    state.value = ViewModelState.error(it.message)
                                })

        addDisposable(disposable)

    }

    private fun getQueryParameterFromUri(url: String?, queryName: String): String {
        val uri = URI(url)
        val query = uri.query
        val parameters = query.split("&")

        return parameters
                .firstOrNull { it.startsWith(queryName) }
                ?.substring(queryName.length + 1)
                ?: ""
    }


}
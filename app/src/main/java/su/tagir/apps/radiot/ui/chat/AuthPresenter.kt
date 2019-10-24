package su.tagir.apps.radiot.ui.chat

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.repository.ChatRepository
import su.tagir.apps.radiot.ui.mvp.BasePresenter
import su.tagir.apps.radiot.ui.mvp.MainDispatcher
import java.net.URI

class AuthPresenter(private val authParams: AuthParams,
                    private val chatRepository: ChatRepository,
                    dispatcher: CoroutineDispatcher = MainDispatcher(),
                    private val router: Router) : BasePresenter<AuthContract.View>(dispatcher), AuthContract.Presenter {

    override fun doOnAttach(view: AuthContract.View) {
        startAuth()
    }

    override fun startAuth() {
        val url = "https://gitter.im/login/oauth/authorize?client_id=${authParams.appId}&" +
                "response_type=${authParams.responseType}&redirect_uri=${authParams.redirectUrl}"
        view?.auth(url)
    }

    override fun requestToken(redirectString: String) {
        if (redirectString.startsWith(authParams.redirectUrl)) {
            launch {
                val code = getQueryParameterFromUri(redirectString, "code")
                view?.showProgress(true)
                chatRepository.getOAuthToken(authParams.appId,
                        authParams.clientId,
                        code,
                        authParams.redirectUrl)
                router.newRootScreen(Screens.ChatScreenFragment)

            }.invokeOnCompletion {
                view?.clearCookies()
                view?.showProgress(false)
            }
        }
    }

    override fun onBackClick() {
        router.exit()
    }

    private fun getQueryParameterFromUri(url: String, queryName: String): String {
        val uri = URI(url)
        val query = uri.query
        val parameters = query.split("&")

        return parameters
                .firstOrNull { it.startsWith(queryName) }
                ?.substring(queryName.length + 1)
                ?: ""
    }
}
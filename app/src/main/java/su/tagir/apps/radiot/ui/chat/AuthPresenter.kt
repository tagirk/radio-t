package su.tagir.apps.radiot.ui.chat

import io.reactivex.Single
import io.reactivex.rxkotlin.plusAssign
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.repository.ChatRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.BasePresenter
import su.tagir.apps.radiot.ui.mvp.Status
import su.tagir.apps.radiot.ui.mvp.ViewState
import timber.log.Timber
import java.net.URI

class AuthPresenter(private val authParams: AuthParams,
                    private val chatRepository: ChatRepository,
                    private val scheduler: BaseSchedulerProvider,
                    private val router: Router): BasePresenter<AuthContract.View>(), AuthContract.Presenter {

    private var state = ViewState(Status.SUCCESS, true)
    set(value) {
        field = value
        view?.updateState(value)
    }

    override fun doOnAttach(view: AuthContract.View) {
        startAuth()
    }

    override fun startAuth() {
        val url =  "https://gitter.im/login/oauth/authorize?client_id=${authParams.appId}&" +
                "response_type=${authParams.responseType}&redirect_uri=${authParams.redirectUrl}"
        view?.auth(url)
    }

    override fun requestToken(redirectString: String) {
        if(redirectString.startsWith(authParams.redirectUrl)) {
            disposables +=
                    Single.fromCallable { getQueryParameterFromUri(redirectString, "code") }
                            .flatMap { code ->
                                chatRepository
                                        .getOAuthToken(
                                                authParams.appId,
                                                authParams.clientId,
                                                code,
                                                authParams.redirectUrl)
                            }
                            .observeOn(scheduler.ui())
                            .doOnSubscribe {  state = ViewState(Status.LOADING) }
                            .subscribe({
                                router.newRootScreen(Screens.ChatScreenFragment)
                                state = ViewState(Status.SUCCESS)
                            },
                                    {
                                        Timber.e(it)
                                        state = ViewState(Status.ERROR, errorMessage = it.message)
                                    })
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
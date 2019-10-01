package su.tagir.apps.radiot.ui.chat

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.repository.ChatRepository
import su.tagir.apps.radiot.ui.common.BackClickHandler
import su.tagir.apps.radiot.ui.mvp.BaseMvpFragment
import su.tagir.apps.radiot.utils.visibleGone
import javax.inject.Inject

class AuthFragment : BaseMvpFragment<AuthContract.View, AuthContract.Presenter>(), AuthContract.View, Injectable, BackClickHandler {

    @Inject
    lateinit var chatRepository: ChatRepository

    @Inject
    lateinit var router: Router

    private lateinit var webView: WebView

    private lateinit var progress: ProgressBar

    private lateinit var toolbar: Toolbar

    private lateinit var retry: Button

    private lateinit var error: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_auth, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = view.findViewById(R.id.web_view)
        progress = view.findViewById(R.id.progress)
        toolbar = view.findViewById(R.id.toolbar)
        retry = view.findViewById(R.id.btn_retry)
        error = view.findViewById(R.id.error)

        retry.setOnClickListener { presenter.startAuth() }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        toolbar.setNavigationOnClickListener { onBackClick() }
        initWebView()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
        webView.resumeTimers()
    }

    override fun onPause() {
        webView.pauseTimers()
        webView.onPause()
        super.onPause()
    }


    override fun onBackClick() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            presenter.onBackClick()
        }
    }

    override fun createPresenter(): AuthContract.Presenter {
        val authParams = AuthParams(getString(R.string.oauth_key), getString(R.string.oauth_secret), getString(R.string.redirect_url), "code")
        return AuthPresenter(authParams, chatRepository, router = router)
    }

    override fun auth(url: String) {
        webView.loadUrl(url)
    }

    override fun showProgress(show: Boolean) {
        progress.visibleGone(show)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progress.visibleGone(true)
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progress.visibleGone(false)
            }

            @Suppress("OverridingDeprecatedMember")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return overrideUrlLoading(url)
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return overrideUrlLoading(request.url.toString())
            }

            private fun overrideUrlLoading(url: String): Boolean {
                presenter.requestToken(url)
                return false
            }
        }
    }
}
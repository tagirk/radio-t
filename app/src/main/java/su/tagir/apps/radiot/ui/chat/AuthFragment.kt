package su.tagir.apps.radiot.ui.chat

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import butterknife.BindView
import butterknife.OnClick
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.ui.common.BaseFragment
import su.tagir.apps.radiot.utils.visibleGone
import javax.inject.Inject


class AuthFragment : BaseFragment(), Injectable {

    @JvmField @BindView(R.id.web_view)
    var webView: WebView? = null

    @JvmField @BindView(R.id.progress)
    var progress: ProgressBar? = null

    @JvmField @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    @JvmField @BindView(R.id.btn_retry)
    var retry: Button? = null

    @JvmField @BindView(R.id.error)
    var error: LinearLayout? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: AuthViewModel

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View =
            inflater.inflate(R.layout.fragment_auth, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(AuthViewModel::class.java)
        toolbar?.setNavigationOnClickListener { onBackPressed() }
        viewModel.authParams = AuthParams(getString(R.string.oauth_key), getString(R.string.oauth_secret), getString(R.string.redirect_url), "code")
        initWebView()
        viewModel.startAuth()
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
        webView?.resumeTimers()
    }

    override fun onPause() {
        webView?.pauseTimers()
        webView?.onPause()
        super.onPause()
    }


    override fun onBackPressed() {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            viewModel.onBackClicked()
        }
    }

    @OnClick(R.id.btn_retry)
    fun retry() {
        viewModel.startAuth()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView?.settings?.javaScriptEnabled = true
        webView?.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progress?.visibleGone(true)
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progress?.visibleGone(false)
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
                return viewModel.redirect(url)
            }
        }
        viewModel.authEvent
                .observe(getViewLifecycleOwner()!!, Observer {
                    webView?.loadUrl(it)
                })

        viewModel.state
                .observe(getViewLifecycleOwner()!!,
                        Observer {
                            progress?.visibleGone(it?.loading == true)
                            error?.visibleGone(it?.error == true)
                        })

        viewModel.message
                .observe(getViewLifecycleOwner()!!,
                        Observer {
                            if (it != null) {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            }
                        })
    }

}
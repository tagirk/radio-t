package su.tagir.apps.radiot.ui.localcontent

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import butterknife.BindView
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.ui.common.BaseFragment
import su.tagir.apps.radiot.utils.longDateFormat
import javax.inject.Inject


class LocalContentFragment : BaseFragment(), Injectable, Toolbar.OnMenuItemClickListener {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: LocalContentViewModel

    @BindView(R.id.web_view)
    lateinit var webView: WebView

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View =
            inflater.inflate(R.layout.fragment_content, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.inflateMenu(R.menu.menu_content)
        toolbar.setOnMenuItemClickListener(this)
        toolbar.setNavigationOnClickListener { onBackPressed() }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(LocalContentViewModel::class.java)

        viewModel.getEntry()
                .observe(getViewLifecycleOwner()!!,
                        Observer { entry ->
                            val sb = "<HTML><HEAD><LINK href=\"material.css\" type=\"text/css\" rel=\"stylesheet\"/></HEAD><body>" +
                                    entry?.body +
                                    "</body></HTML>"
                            webView.loadDataWithBaseURL("file:///android_asset/", sb, "text/html", "utf-8", null)

                            toolbar.title = entry?.title
                            toolbar.subtitle = entry?.date?.longDateFormat()
                        })

        viewModel.setId(arguments?.getString(ARG_ID))
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.web -> viewModel.openInBrowser()
        }
        return false
    }

    companion object {
        const val ARG_ID = "entry_id"

        fun newInstance(url: String?): LocalContentFragment{
            val args = Bundle()
            args.putString(ARG_ID, url)
            val fragment = LocalContentFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
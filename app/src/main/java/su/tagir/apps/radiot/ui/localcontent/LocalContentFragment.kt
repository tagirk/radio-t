package su.tagir.apps.radiot.ui.localcontent

import android.os.Bundle
import android.view.*
import android.webkit.WebView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import butterknife.BindView
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.ui.MainViewModel
import su.tagir.apps.radiot.ui.common.BaseFragment
import javax.inject.Inject


class LocalContentFragment : BaseFragment(), Injectable {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: LocalContentViewModel
    private lateinit var mainViewModel: MainViewModel

    @BindView(R.id.web_view)
    lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View =
            inflater.inflate(R.layout.fragment_content, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView.setBackgroundColor(ContextCompat.getColor(view.context, R.color.colorBackground))

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(LocalContentViewModel::class.java)
        mainViewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)

        viewModel.getEntry()
                .observe(getViewLifecycleOwner()!!,
                        Observer { entry ->
                            val sb = "<HTML><HEAD><LINK href=\"material.css\" type=\"text/css\" rel=\"stylesheet\"/></HEAD><body>" +
                                    entry?.body +
                                    "</body></HTML>"

                            webView.loadDataWithBaseURL("file:///android_asset/", sb, "text/html", "utf-8", null)
                            mainViewModel.setCurrentScreen(entry?.title?:"")
                        })

        viewModel.setId(arguments?.getString(ARG_ID))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_content, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
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
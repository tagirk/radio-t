package su.tagir.apps.radiot.ui.localcontent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.mvp.BaseMvpFragment

class LocalContentFragment : BaseMvpFragment<LocalContentContract.View, LocalContentContract.Presenter>(),
        LocalContentContract.View{

    lateinit var webView: WebView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_content, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = arguments?.getString(ARG_TITLE)
        toolbar.inflateMenu(R.menu.menu_content)
        toolbar.setNavigationOnClickListener { presenter.exit() }
        toolbar.setOnMenuItemClickListener {
            presenter.openInBrowser()
            false
        }

        webView = view.findViewById(R.id.web_view)
        webView.setBackgroundColor(ContextCompat.getColor(view.context, R.color.colorBackground))

    }

    override fun createPresenter(): LocalContentContract.Presenter {

        val appComponent = (activity!!.application as App).appComponent

        return LocalContentPresenter(arguments!!.getString("entry_id")!!,
                appComponent.entryRepository,
                appComponent.router)
    }

    override fun showContent(entry: Entry) {
        val sb = "<HTML><HEAD><LINK href=\"material.css\" type=\"text/css\" rel=\"stylesheet\"/></HEAD><body>" +
                entry.body +
                "</body></HTML>"

        webView.loadDataWithBaseURL("file:///android_asset/", sb, "text/html", "utf-8", null)
    }

    companion object {
        private const val ARG_ID = "entry_id"
        private const val ARG_TITLE = "entry_id"

        fun newInstance(title: String?, url: String?): LocalContentFragment {
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_ID, url)
            val fragment = LocalContentFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
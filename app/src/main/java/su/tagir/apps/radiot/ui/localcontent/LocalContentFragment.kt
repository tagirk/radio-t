package su.tagir.apps.radiot.ui.localcontent

import android.os.Bundle
import android.view.*
import android.webkit.WebView
import androidx.core.content.ContextCompat
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.BaseMvpFragment
import javax.inject.Inject


class LocalContentFragment : BaseMvpFragment<LocalContentContract.View, LocalContentContract.Presenter>(), LocalContentContract.View, Injectable {

    @Inject
    lateinit var entryRepository: EntryRepository

    @Inject
    lateinit var scheduler: BaseSchedulerProvider

    @Inject
    lateinit var router: Router

    lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View =
            inflater.inflate(R.layout.fragment_content, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = view.findViewById(R.id.web_view)
        webView.setBackgroundColor(ContextCompat.getColor(view.context, R.color.colorBackground))

    }

    override fun createPresenter(): LocalContentContract.Presenter =
            LocalContentPresenter(arguments!!.getString("entry_id")!!, entryRepository, router, scheduler)

    override fun showContent(entry: Entry) {
        val sb = "<HTML><HEAD><LINK href=\"material.css\" type=\"text/css\" rel=\"stylesheet\"/></HEAD><body>" +
                entry.body +
                "</body></HTML>"

        webView.loadDataWithBaseURL("file:///android_asset/", sb, "text/html", "utf-8", null)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_content, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.web -> presenter.openInBrowser()
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
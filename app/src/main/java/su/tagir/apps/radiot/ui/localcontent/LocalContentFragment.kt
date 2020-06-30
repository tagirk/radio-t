package su.tagir.apps.radiot.ui.localcontent

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import by.kirich1409.viewbindingdelegate.viewBinding
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.databinding.FragmentContentBinding
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.mvp.MvpFragment

class LocalContentFragment : MvpFragment<LocalContentContract.View, LocalContentContract.Presenter>(R.layout.fragment_content),
        LocalContentContract.View{

    private val binding: FragmentContentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.title = arguments?.getString(ARG_TITLE)
        binding.toolbar.inflateMenu(R.menu.menu_content)
        binding.toolbar.setNavigationOnClickListener { presenter.exit() }
        binding.toolbar.setOnMenuItemClickListener {
            presenter.openInBrowser()
            false
        }

        binding.webView.setBackgroundColor(ContextCompat.getColor(view.context, R.color.colorBackground))

    }

    override fun createPresenter(): LocalContentContract.Presenter {

        val appComponent = (requireActivity().application as App).appComponent

        return LocalContentPresenter(requireArguments().getString("entry_id")!!,
                appComponent.entryRepository,
                appComponent.router)
    }

    override fun showContent(entry: Entry) {
        val sb = "<HTML><HEAD><LINK href=\"material.css\" type=\"text/css\" rel=\"stylesheet\"/></HEAD><body>" +
                entry.body +
                "</body></HTML>"

        binding.webView.loadDataWithBaseURL("file:///android_asset/", sb, "text/html", "utf-8", null)
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
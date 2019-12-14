package su.tagir.apps.radiot.ui.podcasts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.di.AppComponent
import su.tagir.apps.radiot.ui.mvp.BaseMvpFragment
import su.tagir.apps.radiot.ui.podcasts.downloaded.DownloadedPodcastsFragment
import su.tagir.apps.radiot.utils.visibleGone

class PodcastsTabsFragment: BaseMvpFragment<PodcastsTabsContract.View, PodcastsTabsContract.Presenter>(), PodcastsTabsContract.View,
        View.OnClickListener{

    private lateinit var viewPager: ViewPager

    private lateinit var tabs: TabLayout

    private lateinit var streamView: View
    private lateinit var streamTime: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_tabs_podcasts, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.search).setOnClickListener(this)

        viewPager = view.findViewById(R.id.view_pager)
        tabs = view.findViewById(R.id.tabs)
        streamView = view.findViewById(R.id.stream_layout)
        streamTime = view.findViewById(R.id.stream_time)
        streamView.setOnClickListener { presenter.playStream() }
        initFragments()
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.search -> {
                val appComponent: AppComponent = (activity!!.application as App).appComponent
                appComponent.router.navigateTo(Screens.SearchScreen)
            }
        }
    }


    private fun initFragments() {
        val fragmentAdapter = FragmentAdapter(childFragmentManager)
        viewPager.adapter = fragmentAdapter
        tabs.setupWithViewPager(viewPager)
        tabs.getTabAt(0)?.setText(R.string.all)
        tabs.getTabAt(1)?.setText(R.string.downloaded)
    }

    override fun createPresenter(): PodcastsTabsContract.Presenter {
        val entryRepository = (activity!!.application as App).appComponent.entryRepository
        return PodcastTabsPresenter(entryRepository, application = activity!!.application)
    }

    override fun showOrHideStream(show: Boolean) {
        streamView.visibleGone(show)
    }

    override fun showStreamTime(time: String?) {
        streamTime.text = time
    }

    private class FragmentAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {


        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> PodcastsFragment()
                else -> DownloadedPodcastsFragment()
            }
        }

        override fun getCount() = 2
    }
}
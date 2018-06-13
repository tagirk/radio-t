package su.tagir.apps.radiot.ui.podcasts

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.OnPageChange
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.ui.MainViewModel
import su.tagir.apps.radiot.ui.common.BaseFragment
import su.tagir.apps.radiot.ui.podcasts.downloaded.DownloadedPodcastsFragment
import javax.inject.Inject

class PodcastTabsFragment: BaseFragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @BindView(R.id.view_pager)
    lateinit var viewPager: ViewPager

    @BindView(R.id.tabs)
    lateinit var tabs: TabLayout

    private lateinit var podcastsViewModel: PodcastsViewModel
    private lateinit var mainViewModel: MainViewModel

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View=
            inflater.inflate(R.layout.fragment_podcasts, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFragments()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        podcastsViewModel = getViewModel(PodcastsViewModel::class.java)
        mainViewModel = getViewModel(MainViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.setCurrentScreen(Screens.PODCASTS_SCREEN)
        updateData(viewPager.currentItem)
    }

    @OnPageChange(R.id.view_pager)
    fun onPageChange(position: Int) {
        updateData(position)
    }

    private fun initFragments() {
        val fragmentAdapter = FragmentAdapter(childFragmentManager)
        viewPager.adapter = fragmentAdapter
        tabs.setupWithViewPager(viewPager)
    }

    private fun <T : ViewModel> getViewModel(clazz: Class<T>): T {
        return ViewModelProviders.of(activity!!, viewModelFactory).get(clazz)
    }

    private fun updateData(position: Int) {
        when (position) {
            0 -> podcastsViewModel.loadData()
        }
    }

    private class FragmentAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {


        override fun getItem(position: Int): Fragment? {
            when (position) {
                0 -> return PodcastsFragment()
                1 -> return DownloadedPodcastsFragment()
            }
            return null
        }

        override fun getCount() = 2

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "Все"
                1 -> return "Загруженные"
            }
            return super.getPageTitle(position)
        }

    }
}
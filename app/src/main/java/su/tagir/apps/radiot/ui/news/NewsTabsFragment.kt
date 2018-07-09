package su.tagir.apps.radiot.ui.news

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
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.ui.MainViewModel
import su.tagir.apps.radiot.ui.common.BaseFragment
import javax.inject.Inject

class NewsTabsFragment: BaseFragment(), Injectable {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @BindView(R.id.view_pager)
    lateinit var viewPager: ViewPager

    @BindView(R.id.tabs)
    lateinit var tabs: TabLayout

    private lateinit var mainViewModel: MainViewModel

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View =
            inflater.inflate(R.layout.fragment_tabs, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFragments()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainViewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.setCurrentScreen(Screens.NEWS_SCREEN)
    }

    private fun initFragments() {
        val fragmentAdapter = FragmentAdapter(childFragmentManager)
        viewPager.adapter = fragmentAdapter
        tabs.setupWithViewPager(viewPager)
    }

    private class FragmentAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {


        override fun getItem(position: Int): Fragment? {
            when (position) {
                0 -> return ArticlesFragment()
                1 -> return NewsFragment()
            }
            return null
        }

        override fun getCount() = 2

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "Темы от авторов"
                1 -> return "Новости подкаста"
            }
            return super.getPageTitle(position)
        }

    }
}
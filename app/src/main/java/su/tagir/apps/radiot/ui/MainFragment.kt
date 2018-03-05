package su.tagir.apps.radiot.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.OnClick
import butterknife.OnPageChange
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.STREAM_URL
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.ui.common.BaseFragment
import su.tagir.apps.radiot.ui.news.NewsFragment
import su.tagir.apps.radiot.ui.news.NewsViewModel
import su.tagir.apps.radiot.ui.player.PlayerViewModel
import su.tagir.apps.radiot.ui.podcasts.PodcastsFragment
import su.tagir.apps.radiot.ui.podcasts.PodcastsViewModel
import su.tagir.apps.radiot.ui.stream.StreamFragment
import su.tagir.apps.radiot.ui.stream.StreamViewModel
import javax.inject.Inject

class MainFragment : BaseFragment(), Injectable, Toolbar.OnMenuItemClickListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @BindView(R.id.view_pager)
    lateinit var viewPager: ViewPager

    @BindView(R.id.tabs)
    lateinit var tabs: TabLayout

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.btn_play_stream)
    lateinit var btnStream: FloatingActionButton

    @Inject
    lateinit var router: Router

    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var podcastsViewModel: PodcastsViewModel
    private lateinit var newsViewModel: NewsViewModel
    private lateinit var streamViewModel: StreamViewModel
//    private lateinit var hostsViewModel: HostsViewModel

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View =
            inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.inflateMenu(R.menu.menu_main)
        toolbar.setOnMenuItemClickListener(this)
        initFragments()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        playerViewModel = getViewModel(PlayerViewModel::class.java)
        podcastsViewModel = getViewModel(PodcastsViewModel::class.java)
        newsViewModel = getViewModel(NewsViewModel::class.java)
        streamViewModel = getViewModel(StreamViewModel::class.java)
//        hostsViewModel = getViewModel(HostsViewModel::class.java)

        observe()
    }

    override fun onResume() {
        super.onResume()
        updateData(viewPager.currentItem)
    }

    override fun onBackPressed() {
        router.finishChain()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.search -> router.navigateTo(Screens.SEARCH_SCREEN)
            R.id.chat -> playerViewModel.showStreamChat()
            R.id.settings -> router.navigateTo(Screens.SETTINGS_SCREEN)
        }
        return false
    }

    @OnPageChange(R.id.view_pager)
    fun onPageChange(position: Int) {
        updateData(position)
        showHideBtnStream(position == 2 && playerViewModel.getCurrentPodcast().value?.url != STREAM_URL)
    }

    @OnClick(R.id.btn_play_stream)
    fun playStream() {
        playerViewModel.onPlayStreamClick()
    }

    private fun initFragments() {
        val fragmentAdapter = FragmentAdapter(childFragmentManager)
        viewPager.adapter = fragmentAdapter
        tabs.setupWithViewPager(viewPager)
    }

    private fun updateData(position: Int) {
        when (position) {
            0 -> podcastsViewModel.loadData()
            1 -> streamViewModel.loadData()
            2 -> newsViewModel.loadData()
//            3 -> hostsViewModel.loadData()
        }
    }

    private fun observe() {
        playerViewModel
                .getCurrentPodcast()
                .observe(getViewLifecycleOwner()!!,
                        Observer {
                            showHideBtnStream(it?.url != STREAM_URL && viewPager.currentItem == 2)
                        })
    }

    private fun showHideBtnStream(show: Boolean) {
        if (show) {
            btnStream.show()
        } else {
            btnStream.hide()
        }
    }

    private fun <T : ViewModel> getViewModel(clazz: Class<T>): T {
        return ViewModelProviders.of(activity!!, viewModelFactory).get(clazz)
    }

    private class FragmentAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {


        override fun getItem(position: Int): Fragment? {
            when (position) {
                0 -> return PodcastsFragment()
                1 -> return StreamFragment()
                2 -> return NewsFragment()
//                3 -> return HostsFragment()
            }
            return null
        }

        override fun getCount() = 3

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "Подкасты"
                1 -> return "Online вещание"
                2 -> return "Новости"
//                3 -> return "Ведущие"
            }
            return super.getPageTitle(position)
        }

    }
}
package su.tagir.apps.radiot.ui

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.graphics.drawable.DrawerArrowDrawable
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.ui.common.BaseFragment
import su.tagir.apps.radiot.ui.news.NewsViewModel
import su.tagir.apps.radiot.ui.pirates.PiratesViewModel
import su.tagir.apps.radiot.ui.player.PlayerViewModel
import su.tagir.apps.radiot.ui.podcasts.PodcastsViewModel
import su.tagir.apps.radiot.ui.stream.ArticlesViewModel
import javax.inject.Inject

class MainFragment : BaseFragment(), Injectable, Toolbar.OnMenuItemClickListener, NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @BindView(R.id.drawer_layout)
    lateinit var drawerLayout: DrawerLayout

    @BindView(R.id.nav_view)
    lateinit var navigationView: NavigationView

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    private lateinit var homeDrawable: DrawerArrowDrawable

    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var podcastsViewModel: PodcastsViewModel
    private lateinit var newsViewModel: NewsViewModel
    private lateinit var articlesViewModel: ArticlesViewModel
    private lateinit var piratesViewModel: PiratesViewModel



    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View =
            inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        toolbar.inflateMenu(R.menu.menu_main)
//        toolbar.setOnMenuItemClickListener(this)

        navigationView.setNavigationItemSelectedListener(this)
//        initFragments()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        playerViewModel = getViewModel(PlayerViewModel::class.java)
        podcastsViewModel = getViewModel(PodcastsViewModel::class.java)
        newsViewModel = getViewModel(NewsViewModel::class.java)
        articlesViewModel = getViewModel(ArticlesViewModel::class.java)
        piratesViewModel = getViewModel(PiratesViewModel::class.java)

    }

    override fun onResume() {
        super.onResume()
//        updateData(viewPager.currentItem)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
//            R.id.search -> router.navigateTo(Screens.SEARCH_SCREEN)
//            R.id.chat -> playerViewModel.showStreamChat()
//            R.id.settings -> router.navigateTo(Screens.SETTINGS_SCREEN)
        }
        return false
    }


    private fun updateData(position: Int) {
        when (position) {
            0 -> podcastsViewModel.loadData()
            1 -> articlesViewModel.loadData()
            2 -> newsViewModel.loadData()
            3 -> piratesViewModel.loadData()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return false
    }

    private fun <T : ViewModel> getViewModel(clazz: Class<T>): T {
        return ViewModelProviders.of(activity!!, viewModelFactory).get(clazz)
    }

}
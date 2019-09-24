package su.tagir.apps.radiot.ui.podcasts

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.ui.FragmentsInteractionListener
import su.tagir.apps.radiot.ui.podcasts.downloaded.DownloadedPodcastsFragment
import javax.inject.Inject

class PodcastsTabsFragment: Fragment(),
        Toolbar.OnMenuItemClickListener,
        Injectable {

    @Inject
    lateinit var router: Router

    private lateinit var viewPager: ViewPager

    private lateinit var tabs: TabLayout

    private var interactionListener: FragmentsInteractionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        interactionListener = context as FragmentsInteractionListener
    }

    override fun onDetach() {
        interactionListener = null
        super.onDetach()
    }

    override fun onResume() {
        super.onResume()
        interactionListener?.unlockDrawer()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_tabs, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.podcasts)
        toolbar.inflateMenu(R.menu.menu_main)
        toolbar.setOnMenuItemClickListener(this)
        toolbar.setNavigationOnClickListener { interactionListener?.showDrawer() }

        viewPager = view.findViewById(R.id.view_pager)
        tabs = view.findViewById(R.id.tabs)

        initFragments()
    }

    override fun onMenuItemClick(p0: MenuItem?): Boolean {
        when(p0?.itemId){
            R.id.search -> router.navigateTo(Screens.SearchScreen)
        }
        return false
    }


    private fun initFragments() {
        val fragmentAdapter = FragmentAdapter(childFragmentManager)
        viewPager.adapter = fragmentAdapter
        tabs.setupWithViewPager(viewPager)
    }

    private class FragmentAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {


        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> PodcastsFragment()
                else -> DownloadedPodcastsFragment()
            }
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
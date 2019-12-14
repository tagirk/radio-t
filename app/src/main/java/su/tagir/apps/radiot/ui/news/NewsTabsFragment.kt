package su.tagir.apps.radiot.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.utils.visibleGone

class NewsTabsFragment : Fragment() {

    private lateinit var viewPager: ViewPager

    private lateinit var tabs: TabLayout


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tabs_podcasts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.search).visibleGone(false)

        viewPager = view.findViewById(R.id.view_pager)
        viewPager.offscreenPageLimit = 2
        tabs = view.findViewById(R.id.tabs)
        initFragments()
    }

    private fun initFragments() {
        val fragmentAdapter = FragmentAdapter(childFragmentManager)
        viewPager.adapter = fragmentAdapter
        tabs.setupWithViewPager(viewPager)
        tabs.getTabAt(0)?.setText(R.string.themes)
        tabs.getTabAt(1)?.setText(R.string.themes_from_authors)
        tabs.getTabAt(2)?.setText(R.string.news)
    }

    private class FragmentAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {


        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> NewsFragment.newInstance(arrayOf("prep"))
                1 -> ArticlesFragment()
                else -> NewsFragment.newInstance(arrayOf("news", "info"))
            }
        }

        override fun getCount() = 3

    }
}
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
        tabs = view.findViewById(R.id.tabs)
        initFragments()
    }

    private fun initFragments() {
        val fragmentAdapter = FragmentAdapter(childFragmentManager)
        viewPager.adapter = fragmentAdapter
        tabs.setupWithViewPager(viewPager)
    }

    private class FragmentAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {


        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> ArticlesFragment()
                else -> NewsFragment()
            }
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
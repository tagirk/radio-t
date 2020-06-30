package su.tagir.apps.radiot.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.databinding.FragmentTabsPodcastsBinding
import su.tagir.apps.radiot.utils.visibleGone

class NewsTabsFragment : Fragment() {

    private lateinit var binding: FragmentTabsPodcastsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTabsPodcastsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.search.visibleGone(false)

        binding.viewPager.offscreenPageLimit = 2
        initFragments()
    }

    private fun initFragments() {
        val fragmentAdapter = FragmentAdapter(childFragmentManager)
        binding.viewPager.adapter = fragmentAdapter
        binding.tabs.setupWithViewPager(binding.viewPager)
        binding.tabs.getTabAt(0)?.setText(R.string.themes)
        binding.tabs.getTabAt(1)?.setText(R.string.themes_from_authors)
        binding.tabs.getTabAt(2)?.setText(R.string.news)
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
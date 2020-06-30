package su.tagir.apps.radiot.ui.pirates

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.databinding.FragmentTabsPodcastsBinding
import su.tagir.apps.radiot.ui.pirates.downloaded.DownloadedPiratesFragment
import su.tagir.apps.radiot.utils.visibleGone

class PiratesTabsFragment: Fragment(R.layout.fragment_tabs_podcasts){

    private val binding: FragmentTabsPodcastsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.search.visibleGone(false)
        initFragments()
    }


    private fun initFragments() {
        val fragmentAdapter = FragmentAdapter(childFragmentManager)
        binding.viewPager.adapter = fragmentAdapter
        binding.tabs.setupWithViewPager(binding.viewPager)
        binding.tabs.getTabAt(0)?.setText(R.string.all)
        binding.tabs.getTabAt(1)?.setText(R.string.downloaded)
    }

    private class FragmentAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {


        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> PiratesFragment()
                else -> DownloadedPiratesFragment()
            }
        }

        override fun getCount() = 2

    }
}
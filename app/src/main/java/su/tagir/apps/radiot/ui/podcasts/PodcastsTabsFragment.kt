package su.tagir.apps.radiot.ui.podcasts

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.databinding.FragmentTabsPodcastsBinding
import su.tagir.apps.radiot.di.AppComponent
import su.tagir.apps.radiot.ui.mvp.MvpFragment
import su.tagir.apps.radiot.ui.podcasts.downloaded.DownloadedPodcastsFragment
import su.tagir.apps.radiot.utils.visibleGone

class PodcastsTabsFragment: MvpFragment<PodcastsTabsContract.View, PodcastsTabsContract.Presenter>(R.layout.fragment_tabs_podcasts), PodcastsTabsContract.View,
        View.OnClickListener{

    private val binding: FragmentTabsPodcastsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.search.setOnClickListener(this)

        binding.stream.setOnClickListener { presenter.playStream() }
        initFragments()
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.search -> {
                val appComponent: AppComponent = (requireActivity().application as App).appComponent
                appComponent.router.navigateTo(Screens.SearchScreen)
            }
        }
    }

    private fun initFragments() {
        val fragmentAdapter = FragmentAdapter(childFragmentManager)
        binding.viewPager.adapter = fragmentAdapter
        binding.tabs.setupWithViewPager(binding.viewPager)
        binding.tabs.getTabAt(0)?.setText(R.string.all)
        binding.tabs.getTabAt(1)?.setText(R.string.downloaded)
    }

    override fun createPresenter(): PodcastsTabsContract.Presenter {
        val entryRepository = (requireActivity().application as App).appComponent.entryRepository
        return PodcastTabsPresenter(entryRepository, application = requireActivity().application)
    }

    override fun showOrHideStream(show: Boolean) {
        binding.stream.visibleGone(show)
    }

    override fun showStreamTime(time: String?) {
        binding.stream.text = time
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
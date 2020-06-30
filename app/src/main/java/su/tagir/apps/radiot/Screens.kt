package su.tagir.apps.radiot

import androidx.fragment.app.Fragment
import ru.terrakok.cicerone.android.support.SupportAppScreen
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.comments.CommentsFragment
import su.tagir.apps.radiot.ui.localcontent.LocalContentFragment
import su.tagir.apps.radiot.ui.news.NewsTabsFragment
import su.tagir.apps.radiot.ui.pirates.PiratesTabsFragment
import su.tagir.apps.radiot.ui.podcasts.PodcastsTabsFragment
import su.tagir.apps.radiot.ui.search.SearchFragment
import su.tagir.apps.radiot.ui.settings.AboutFragmentRoot
import su.tagir.apps.radiot.ui.settings.CreditsFragment
import su.tagir.apps.radiot.ui.settings.SettingsFragmentRoot


object Screens {

    const val WEB_SCREEN = "web_screen"
    const val RESOLVE_ACTIVITY = "resolve_activity"

    object PdcastsScreen: SupportAppScreen(){
        override fun getFragment(): Fragment {
            return PodcastsTabsFragment()
        }
    }

    object NewsScreen: SupportAppScreen(){
        override fun getFragment(): Fragment {
            return NewsTabsFragment()
        }
    }

    object SearchScreen: SupportAppScreen(){
        override fun getFragment(): Fragment {
            return SearchFragment()
        }
    }

    data class ContentScreen(val title: String?, val url: String): SupportAppScreen(){
        override fun getFragment(): Fragment {
            return LocalContentFragment.newInstance(title, url)
        }
    }

    object SettingsScreen: SupportAppScreen(){
        override fun getFragment(): Fragment {
            return SettingsFragmentRoot()
        }
    }

    object PiratesScreen: SupportAppScreen(){
        override fun getFragment(): Fragment {
            return PiratesTabsFragment()
        }
    }

    object CreditsScreen: SupportAppScreen(){
        override fun getFragment(): Fragment {
            return CreditsFragment()
        }
    }

    object AboutScreen: SupportAppScreen(){
        override fun getFragment(): Fragment {
            return AboutFragmentRoot()
        }
    }

    data class CommentsScreen(val entry: Entry): SupportAppScreen(){
        override fun getFragment(): Fragment {
            return CommentsFragment.newInstance(entry)
        }
    }

    data class WebScreen(val url: String): SupportAppScreen(){

        override fun getScreenKey(): String {
            return WEB_SCREEN
        }
    }

    data class ResolveActivityScreen(val url: String): SupportAppScreen(){
        override fun getScreenKey(): String {
            return RESOLVE_ACTIVITY
        }
    }
}
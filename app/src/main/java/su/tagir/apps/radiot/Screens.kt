package su.tagir.apps.radiot

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import ru.terrakok.cicerone.android.support.SupportAppScreen
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.chat.AuthFragment
import su.tagir.apps.radiot.ui.chat.ChatActivity
import su.tagir.apps.radiot.ui.chat.ChatFragment
import su.tagir.apps.radiot.ui.comments.CommentsFragment
import su.tagir.apps.radiot.ui.localcontent.LocalContentFragment
import su.tagir.apps.radiot.ui.news.NewsTabsFragment
import su.tagir.apps.radiot.ui.pirates.PiratesTabsFragment
import su.tagir.apps.radiot.ui.podcasts.PodcastsTabsFragment
import su.tagir.apps.radiot.ui.search.SearchFragment
import su.tagir.apps.radiot.ui.settings.AboutFragment
import su.tagir.apps.radiot.ui.settings.CreditsFragment
import su.tagir.apps.radiot.ui.settings.SettingsFragmentRoot


object Screens {

    const val PODCASTS_SCREEN = "podcasts_screen"
    const val NEWS_SCREEN = "news_screen"
    const val SEARCH_SCREEN  = "search_screen"
    const val CONTENT_SCREEN = "content_screen"
    const val WEB_SCREEN = "web_screen"
    const val RESOLVE_ACTIVITY = "resolve_activity"
    const val SETTINGS_SCREEN = "settings_screen"
    const val ABOUT_SCREEN = "about_screen"

    const val PIRATES_SCREEN = "pirates_screen"
    const val CREDITS_SCREEN = "credits_screen"
    const val COMMENTS_SCREEN = "comments_screen"

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
            return AboutFragment()
        }
    }

    data class CommentsScreen(val entry: Entry): SupportAppScreen(){
        override fun getFragment(): Fragment {
            return CommentsFragment.newInstance(entry)
        }
    }

    object ChatScreen: SupportAppScreen(){
        override fun getActivityIntent(context: Context?): Intent {
            return Intent(context, ChatActivity::class.java)
        }
    }

    object ChatAuthScreen: SupportAppScreen(){
        override fun getFragment(): Fragment {
            return AuthFragment()
        }
    }

    object ChatScreenFragment: SupportAppScreen(){
        override fun getFragment(): Fragment {
            return ChatFragment()
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
package su.tagir.apps.radiot.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import su.tagir.apps.radiot.ui.chat.AuthFragment
import su.tagir.apps.radiot.ui.chat.ChatFragment
import su.tagir.apps.radiot.ui.comments.CommentsFragment
import su.tagir.apps.radiot.ui.localcontent.LocalContentFragment
import su.tagir.apps.radiot.ui.news.ArticlesFragment
import su.tagir.apps.radiot.ui.news.NewsFragment
import su.tagir.apps.radiot.ui.news.NewsTabsFragment
import su.tagir.apps.radiot.ui.pirates.PiratesFragment
import su.tagir.apps.radiot.ui.pirates.PiratesTabsFragment
import su.tagir.apps.radiot.ui.pirates.downloaded.DownloadedPiratesFragment
import su.tagir.apps.radiot.ui.player.PlayerFragment
import su.tagir.apps.radiot.ui.podcasts.PodcastsFragment
import su.tagir.apps.radiot.ui.podcasts.PodcastsTabsFragment
import su.tagir.apps.radiot.ui.podcasts.downloaded.DownloadedPodcastsFragment
import su.tagir.apps.radiot.ui.search.SearchFragment
import su.tagir.apps.radiot.ui.settings.*

@Module
abstract class FragmentsBuilderModule {

    @ContributesAndroidInjector
    abstract fun podcastTabsFragment(): PodcastsTabsFragment

    @ContributesAndroidInjector
    abstract fun podcastsFragment(): PodcastsFragment

    @ContributesAndroidInjector
    abstract fun downloadedPodcastsFragment(): DownloadedPodcastsFragment

    @ContributesAndroidInjector
    abstract fun piratesTabsFragment(): PiratesTabsFragment

    @ContributesAndroidInjector
    abstract fun piratesFragment(): PiratesFragment

    @ContributesAndroidInjector
    abstract fun downloadedPiratesFragment(): DownloadedPiratesFragment

    @ContributesAndroidInjector
    abstract fun playerFragment(): PlayerFragment

    @ContributesAndroidInjector
    abstract fun newsTabsFragment(): NewsTabsFragment

    @ContributesAndroidInjector
    abstract fun newsFragment(): NewsFragment

    @ContributesAndroidInjector
    abstract fun searchFragment(): SearchFragment

    @ContributesAndroidInjector
    abstract fun streamFragment(): ArticlesFragment

    @ContributesAndroidInjector
    abstract fun contentFragment(): LocalContentFragment

    @ContributesAndroidInjector
    abstract fun authFragment(): AuthFragment

    @ContributesAndroidInjector
    abstract fun chatFragment(): ChatFragment

    @ContributesAndroidInjector
    abstract fun settingsFragmentRoot(): SettingsFragmentRoot

    @ContributesAndroidInjector
    abstract fun settingsFragment(): SettingsFragment

    @ContributesAndroidInjector
    abstract fun aboutFragment(): AboutFragment

    @ContributesAndroidInjector
    abstract fun aboutFragmentRoot(): AboutFragmentRoot

    @ContributesAndroidInjector
    abstract fun creditsFragment(): CreditsFragment

    @ContributesAndroidInjector
    abstract fun commentsFragment(): CommentsFragment
}
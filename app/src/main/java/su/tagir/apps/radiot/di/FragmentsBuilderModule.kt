package su.tagir.apps.radiot.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import su.tagir.apps.radiot.ui.MainFragment
import su.tagir.apps.radiot.ui.chat.AuthFragment
import su.tagir.apps.radiot.ui.chat.ChatFragment
import su.tagir.apps.radiot.ui.hosts.HostsFragment
import su.tagir.apps.radiot.ui.localcontent.LocalContentFragment
import su.tagir.apps.radiot.ui.news.NewsFragment
import su.tagir.apps.radiot.ui.pirates.PiratesFragment
import su.tagir.apps.radiot.ui.player.PlayerFragment
import su.tagir.apps.radiot.ui.podcasts.PodcastsFragment
import su.tagir.apps.radiot.ui.search.SearchFragment
import su.tagir.apps.radiot.ui.stream.StreamFragment

@Module
abstract class FragmentsBuilderModule {

    @ContributesAndroidInjector
    abstract fun mainFragment(): MainFragment

    @ContributesAndroidInjector
    abstract fun podcastsFragment(): PodcastsFragment

    @ContributesAndroidInjector
    abstract fun playerFragment(): PlayerFragment

    @ContributesAndroidInjector
    abstract fun newsFragment(): NewsFragment

    @ContributesAndroidInjector
    abstract fun searchFragment(): SearchFragment

    @ContributesAndroidInjector
    abstract fun streamFragment(): StreamFragment

    @ContributesAndroidInjector
    abstract fun hostsFragment(): HostsFragment

    @ContributesAndroidInjector
    abstract fun contentFragment(): LocalContentFragment

    @ContributesAndroidInjector
    abstract fun authFragment(): AuthFragment

    @ContributesAndroidInjector
    abstract fun chatFragment(): ChatFragment

    @ContributesAndroidInjector
    abstract fun piratesFragment(): PiratesFragment
}
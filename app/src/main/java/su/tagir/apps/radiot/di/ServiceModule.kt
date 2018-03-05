package su.tagir.apps.radiot.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import su.tagir.apps.radiot.model.EntryContentProvider
import su.tagir.apps.radiot.model.PodcastStateService

@Module
abstract class ServiceModule {

    @ContributesAndroidInjector
    abstract fun entryContentProvider(): EntryContentProvider

    @ContributesAndroidInjector
    abstract fun entryStateService(): PodcastStateService


}
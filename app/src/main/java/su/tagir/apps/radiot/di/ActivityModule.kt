package su.tagir.apps.radiot.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import su.tagir.apps.radiot.ui.MainActivity
import su.tagir.apps.radiot.ui.chat.ChatActivity

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    (modules = [FragmentsBuilderModule::class])
    abstract fun mainActivity(): MainActivity


    @ContributesAndroidInjector
    (modules = [ChatFragmentsBuilderModule::class])
    abstract fun chatActivity(): ChatActivity
}
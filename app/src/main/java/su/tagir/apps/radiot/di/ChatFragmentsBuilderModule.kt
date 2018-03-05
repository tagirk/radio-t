package su.tagir.apps.radiot.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import su.tagir.apps.radiot.ui.chat.AuthFragment
import su.tagir.apps.radiot.ui.chat.ChatFragment

@Module
abstract class ChatFragmentsBuilderModule {

    @ContributesAndroidInjector
    abstract fun authFragment(): AuthFragment

    @ContributesAndroidInjector
    abstract fun chatFragment(): ChatFragment

}
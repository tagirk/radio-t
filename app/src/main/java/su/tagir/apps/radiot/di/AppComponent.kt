package su.tagir.apps.radiot.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import su.tagir.apps.radiot.App
import javax.inject.Singleton

@Singleton
@Component(modules =
[AndroidSupportInjectionModule::class,
    AppModule::class,
    DbModule::class,
    ActivityModule::class,
    ServiceModule::class,
    NavigationModule::class])

interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(app: App)

}
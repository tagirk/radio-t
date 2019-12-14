package su.tagir.apps.radiot.di


interface AppComponent : AppModule, DataModule, NavigationModule {

    val appModule: AppModule

    val dataModule: DataModule

    val navigationModule: NavigationModule

    class Impl(override val appModule: AppModule,
               override val dataModule: DataModule,
               override val navigationModule: NavigationModule) :
            AppComponent,
            AppModule by appModule,
            DataModule by dataModule,
            NavigationModule by navigationModule
}
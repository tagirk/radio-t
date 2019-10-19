package su.tagir.apps.radiot.di

import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router

interface NavigationModule {

    val cicerone: Cicerone<Router>

    val router: Router

    val navigatorHolder: NavigatorHolder

    class Impl: NavigationModule {

        override val cicerone: Cicerone<Router> by lazy { Cicerone.create() }

        override val router : Router = cicerone.router

        override val navigatorHolder: NavigatorHolder = cicerone.navigatorHolder

    }
}
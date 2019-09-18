package su.tagir.apps.radiot.ui.search

import dagger.Component
import su.tagir.apps.radiot.di.AppComponent
import su.tagir.apps.radiot.di.NavigationModule
import javax.inject.Singleton

@Singleton
@Component(dependencies = [AppComponent::class], modules = [NavigationModule::class])
interface SearchComponent{

    fun presenter(): SearchPresenter
}
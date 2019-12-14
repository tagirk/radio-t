package su.tagir.apps.radiot.di

import android.app.Application


interface AppModule {

    val application: Application


    class Impl(override val application: Application) : AppModule
}
package su.tagir.apps.radiot.di

import android.app.Application
import dagger.Module
import dagger.Provides
import su.tagir.apps.radiot.model.db.AppDatabase
import javax.inject.Singleton

@Module
class DbModule {

    @Singleton
    @Provides
    fun provideDb(application: Application) = AppDatabase.createPersistentDatabase(application)

    @Singleton
    @Provides
    fun provideEntryDao(db: AppDatabase) = db.entryDao()

    @Singleton
    @Provides
    fun provideHostsDao(db: AppDatabase) = db.hostDao()

    @Singleton
    @Provides
    fun provideNewsDao(db: AppDatabase) = db.newsDao()

    @Singleton
    @Provides
    fun providesGitterDao(db: AppDatabase) = db.gitterDao()

}
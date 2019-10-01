package su.tagir.apps.radiot.di

import android.app.Application
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import dagger.Module
import dagger.Provides
import su.tagir.apps.radiot.model.db.RadiotDb
import su.tagir.apps.radiot.model.db.Schema
import su.tagir.apps.radiot.model.db.createQueryWrapper
import javax.inject.Singleton

@Module
class DbModule {

    @Singleton
    @Provides
    fun provideDb(application: Application): RadiotDb{
        val driver: SqlDriver = AndroidSqliteDriver(Schema, application, "radiot.db")
        return createQueryWrapper(driver)
    }



}
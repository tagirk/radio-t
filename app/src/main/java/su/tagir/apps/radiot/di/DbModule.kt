package su.tagir.apps.radiot.di

import android.app.Application
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import dagger.Module
import dagger.Provides
import su.tagir.apps.radiot.model.db.*
import javax.inject.Singleton

@Module
class DbModule {


    @Singleton
    @Provides
    fun provideDb(application: Application): RadiotDb{
        val helper = FrameworkSQLiteOpenHelperFactory().create(sqlOpenHelperConfiguration(application))
        val driver: SqlDriver = AndroidSqliteDriver(helper)
        return createQueryWrapper(driver)
    }



}
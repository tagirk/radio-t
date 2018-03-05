package su.tagir.apps.radiot.model.db

import android.arch.paging.DataSource
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import su.tagir.apps.radiot.model.entries.Host

@Dao
abstract class HostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertHosts(hosts:List<Host>)

    @Query("SELECT * FROM host")
    abstract fun findHosts(): DataSource.Factory<Int, Host>
}
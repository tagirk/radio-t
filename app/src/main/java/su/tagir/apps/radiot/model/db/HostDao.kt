package su.tagir.apps.radiot.model.db

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import su.tagir.apps.radiot.model.entries.Host

@Dao
abstract class HostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertHosts(hosts:List<Host>)

    @Query("SELECT * FROM host")
    abstract fun findHosts(): DataSource.Factory<Int, Host>
}
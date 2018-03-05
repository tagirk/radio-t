package su.tagir.apps.radiot.model.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import su.tagir.apps.radiot.model.entries.*


@Database(entities = [
    Entry::class,
    TimeLabel::class,
    SearchResult::class,
    Host::class,
    Article::class,
    User::class,
    Message::class],
        version = 14,
        exportSchema = false)

@TypeConverters(StringListConverter::class,
        DateConverter::class,
        UrlListConverter::class,
        MentionsListConverter::class)

abstract class AppDatabase : RoomDatabase() {

    abstract fun entryDao(): EntryDao

    abstract fun hostDao(): HostDao

    abstract fun newsDao(): NewsDao

    abstract fun gitterDao(): GitterDao

    companion object {
        private const val DB_NAME = "radiot.db"

        fun createInMemoryDatabase(context: Context): AppDatabase = Room.inMemoryDatabaseBuilder(context.applicationContext, AppDatabase::class.java)
                .build()

        fun createPersistentDatabase(context: Context): AppDatabase = Room
                .databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()

    }
}
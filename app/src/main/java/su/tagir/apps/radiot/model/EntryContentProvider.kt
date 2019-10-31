package su.tagir.apps.radiot.model

import android.annotation.SuppressLint
import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.net.Uri
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import su.tagir.apps.radiot.model.db.sqlOpenHelperConfiguration
import su.tagir.apps.radiot.model.entries.EntryState
import timber.log.Timber

class EntryContentProvider : ContentProvider() {

    companion object {
        private const val AUTHORITY = "su.tagir.apps.radiot.contentprovider"

        const val ENTRY_URI = "content://$AUTHORITY/entry"
        const val UPDATE_ENTRY_URI = "content://$AUTHORITY/update_entry"

        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        const val CURRENT_ENTRY = 1
        const val UPDATE_CURRENT_ENTRY = 2

        init {
            uriMatcher.addURI(AUTHORITY, "entry", CURRENT_ENTRY)
            uriMatcher.addURI(AUTHORITY, "update_entry", UPDATE_CURRENT_ENTRY)
        }
    }

    private lateinit var sqlHelper: SupportSQLiteOpenHelper

    override fun onCreate(): Boolean {
        sqlHelper = FrameworkSQLiteOpenHelperFactory().create(sqlOpenHelperConfiguration(context!!))
        return false
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri {
        throw UnsupportedOperationException("insert not implemented")
    }

    @SuppressLint("Recycle")
    override fun query(p0: Uri, p1: Array<out String>?, p2: String?, p3: Array<out String>?, p4: String?): Cursor {
        val cursor = when (uriMatcher.match(p0)) {
            CURRENT_ENTRY -> {
                sqlHelper.readableDatabase.query("SELECT * FROM entry WHERE state = 1 OR state = 2 LIMIT 1")
            }
            else -> throw IllegalArgumentException("Unknown uri: $p0")
        }
        cursor.setNotificationUri(context?.contentResolver, p0)
        return cursor
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        if (p1 == null) {
            return -1
        }

        when (uriMatcher.match(p0)) {
            CURRENT_ENTRY -> {
                val lastProgress = p1.getAsLong("lastProgress")
                val audioUrl = p1.getAsString("audioUrl")
                setCurrentEntry(audioUrl, lastProgress, sqlHelper.writableDatabase)
            }
            UPDATE_CURRENT_ENTRY -> {
                val state = p1.getAsInteger("state")
                val progress = p1.getAsLong("progress")
                updateCurrentEntryStateAndProgress(state, progress, sqlHelper.writableDatabase)
            }
        }
        context?.contentResolver?.notifyChange(p0, null)
        return 1

    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        throw UnsupportedOperationException("Delete not implemented")
    }

    override fun getType(p0: Uri): String? = null

    private fun setCurrentEntry(audioUrl: String?, lastProgress: Long, database: SupportSQLiteDatabase) {
        database.beginTransaction()
        try {
            if (lastProgress > 0) {
                database.execSQL("UPDATE entry SET progress = ? WHERE state = ? OR state = ?", arrayOf(lastProgress, EntryState.PLAYING, EntryState.PAUSED))
            }
            database.execSQL("UPDATE entry SET state = ? WHERE state != ?", arrayOf(EntryState.IDLE, EntryState.IDLE))
            if (audioUrl != null) {
                database.execSQL("UPDATE entry SET state = ? WHERE audioUrl = ?", arrayOf(EntryState.PAUSED, audioUrl))
            }
            database.setTransactionSuccessful()
        } catch (e: SQLException) {
            Timber.e(e)
        } finally {
            database.endTransaction()
        }
    }

    private fun updateCurrentEntryStateAndProgress(state: Int, progress: Long, database: SupportSQLiteDatabase) {
        database.beginTransaction()
        try {
            if (state != EntryState.PLAYING && progress > 0) {
                database.execSQL("UPDATE entry SET progress = ? WHERE state = ? OR state = ?", arrayOf(progress, EntryState.PLAYING, EntryState.PAUSED))
            }
            database.execSQL("UPDATE entry SET state = ? WHERE state != ?", arrayOf(state, EntryState.IDLE))
            database.setTransactionSuccessful()
        } catch (e: SQLException) {
            Timber.e(e)
        } finally {
            database.endTransaction()
        }
    }
}
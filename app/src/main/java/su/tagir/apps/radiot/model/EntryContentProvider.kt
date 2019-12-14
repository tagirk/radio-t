package su.tagir.apps.radiot.model

import android.annotation.SuppressLint
import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import androidx.sqlite.db.SupportSQLiteOpenHelper
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.model.db.sqlOpenHelper
import su.tagir.apps.radiot.model.entries.EntryState

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
        sqlHelper = sqlOpenHelper(context!!)
        return false
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri {
        throw UnsupportedOperationException("insert not implemented")
    }

    @SuppressLint("Recycle")
    override fun query(p0: Uri, p1: Array<out String>?, p2: String?, p3: Array<out String>?, p4: String?): Cursor {
        val cursor = when (uriMatcher.match(p0)) {
            CURRENT_ENTRY -> {
                sqlHelper.readableDatabase.query("SELECT * FROM entry WHERE state = ? OR state = ? LIMIT 1", arrayOf(EntryState.PLAYING, EntryState.PAUSED))
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
        val app = context?.applicationContext as? App
        val database = app?.appComponent?.dataModule?.database
        val entryQueries = database?.entryQueries

        when (uriMatcher.match(p0)) {
            CURRENT_ENTRY -> {
                val lastProgress = p1.getAsLong("lastProgress")
                val audioUrl = p1.getAsString("audioUrl")
                database?.transaction {
                    if (lastProgress > 0) {
                        entryQueries?.updateCurrentPlayingEntryProgress(lastProgress)
                    }
                    entryQueries?.resetStates(EntryState.IDLE, EntryState.IDLE)
                    if (audioUrl != null) {
                        entryQueries?.updateState(EntryState.PAUSED, audioUrl)
                    }
                }
            }
            UPDATE_CURRENT_ENTRY -> {
                val state = p1.getAsInteger("state")
                val progress = p1.getAsLong("progress")
                database?.transaction {
                    if (state != EntryState.PLAYING && progress > 0) {
                        entryQueries?.updateCurrentPlayingEntryProgress(progress)
                    }
                   entryQueries?.updateCurrentPlayingEntryState(state)
                }
            }
            else -> throw IllegalArgumentException("Unknown uri: $p0")
        }
        context?.contentResolver?.notifyChange(p0, null)
        return 1

    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        throw UnsupportedOperationException("Delete not implemented")
    }

    override fun getType(p0: Uri): String? = null
}
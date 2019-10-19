package su.tagir.apps.radiot.model

import android.annotation.SuppressLint
import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import su.tagir.apps.radiot.model.db.sqlOpenHelperConfiguration

class EntryContentProvider : ContentProvider() {

    companion object {
        private const val AUTHORITY = "su.tagir.apps.radiot.contentprovider"

        const val ENTRY_URI = "content://$AUTHORITY/entry"

        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        const val CURRENT_ENTRY = 1

        init {
            uriMatcher.addURI(AUTHORITY, "entry", CURRENT_ENTRY)
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
        cursor.setNotificationUri(context?.contentResolver, p0 )
        return cursor
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
       throw UnsupportedOperationException("update not implemented")
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        throw UnsupportedOperationException("Delete not implemented")
    }

    override fun getType(p0: Uri): String? = null

}
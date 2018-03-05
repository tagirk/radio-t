package su.tagir.apps.radiot.model

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import dagger.android.AndroidInjection
import su.tagir.apps.radiot.model.db.EntryDao
import javax.inject.Inject

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

    @Inject
    lateinit var entryDao: EntryDao

    override fun onCreate(): Boolean {
        return false
    }

    override fun insert(p0: Uri?, p1: ContentValues): Uri {
        throw UnsupportedOperationException("insert not implemented")
    }

    override fun query(p0: Uri?, p1: Array<out String>?, p2: String?, p3: Array<out String>?, p4: String?): Cursor {
        AndroidInjection.inject(this)
        val cursor = when (uriMatcher.match(p0)) {
            CURRENT_ENTRY -> entryDao.getCurrentEntryCursor()

            else -> throw IllegalArgumentException("Unknown uri: " + p0)
        }
        cursor.setNotificationUri(context.contentResolver, p0 )
        return cursor
    }

    override fun update(p0: Uri?, p1: ContentValues, p2: String?, p3: Array<out String>?): Int {
       throw UnsupportedOperationException("update not implemented")
    }

    override fun delete(p0: Uri?, p1: String?, p2: Array<out String>?): Int {
        throw UnsupportedOperationException("Delete not implemented")
    }

    override fun getType(p0: Uri?): String? = null

}
package su.tagir.apps.radiot.model.db

import android.content.Context
import android.text.TextUtils
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import su.tagir.apps.radiot.model.entities.*
import java.util.*

fun sqlOpenHelper(context: Context): SupportSQLiteOpenHelper =
        FrameworkSQLiteOpenHelperFactory()
                .create(SupportSQLiteOpenHelper.Configuration.builder(context)
                        .name("radiot.db")
                        .callback(AndroidSqliteDriver.Callback(Schema))
                        .build())

object Schema: SqlDriver.Schema by RadiotDb.Schema{

    override val version: Int
        get() = 16
}

fun createQueryWrapper(driver: SqlDriver): RadiotDb {
    return RadiotDb.invoke(
            driver = driver,
            articleAdapter = Article.Adapter(dateAdapter = dateAdapter,
                    addedDateAdapter = dateAdapter),
            entryAdapter = Entry.Adapter(dateAdapter = dateAdapter, commentatorsAdapter = stringListAdapter),
            pageResultAdapter = PageResult.Adapter(idsAdapter = stringListAdapter),
            searchResultAdapter = SearchResult.Adapter(idsAdapter = stringListAdapter, timeStampAdapter = dateAdapter),
            time_labelAdapter = Time_label.Adapter(podcast_timeAdapter = dateAdapter)
    )
}

val dateAdapter = object : ColumnAdapter<Date, Long> {
    override fun decode(databaseValue: Long): Date = Date(databaseValue)

    override fun encode(value: Date): Long = value.time
}

val stringListAdapter = object : ColumnAdapter<List<String>, String> {
    override fun decode(databaseValue: String): List<String> = TextUtils.split(databaseValue, ",").toList()

    override fun encode(value: List<String>): String = TextUtils.join(",", value)
}




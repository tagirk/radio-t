package su.tagir.apps.radiot.model.db

import android.content.Context
import android.text.TextUtils
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import org.json.JSONArray
import org.json.JSONObject
import su.tagir.apps.radiot.model.entities.*
import su.tagir.apps.radiot.model.entries.Mention
import su.tagir.apps.radiot.model.entries.Url
import java.util.*

fun sqlOpenHelper(context: Context): SupportSQLiteOpenHelper =
        FrameworkSQLiteOpenHelperFactory()
                .create(SupportSQLiteOpenHelper.Configuration.builder(context)
                        .name("radiot.db")
                        .callback(AndroidSqliteDriver.Callback(Schema))
                        .build())

object Schema: SqlDriver.Schema by RadiotDb.Schema{

    override val version: Int
        get() = 15
}

fun createQueryWrapper(driver: SqlDriver): RadiotDb {
    return RadiotDb.invoke(
            driver = driver,
            articleAdapter = Article.Adapter(dateAdapter = dateAdapter,
                    addedDateAdapter = dateAdapter),
            entryAdapter = Entry.Adapter(dateAdapter = dateAdapter, commentatorsAdapter = stringListAdapter),
            messageAdapter = Message.Adapter(sentAdapter = dateAdapter,
                    editedAtAdapter = dateAdapter,
                    urlsAdapter = urlsAdapter,
                    mentionsAdapter = mentionsAdapter),
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

val urlsAdapter = object : ColumnAdapter<List<Url>, String> {
    override fun decode(databaseValue: String): List<Url> = TextUtils.split(databaseValue, ",").map { Url(it) }

    override fun encode(value: List<Url>): String = TextUtils.join(",", value.map { it.url })
}

val mentionsAdapter = object : ColumnAdapter<List<Mention>, String> {
    override fun decode(databaseValue: String): List<Mention> {
        val jsonObject = JSONObject(databaseValue)
        val array = JSONArray(jsonObject.getString("list"))
        val list = ArrayList<Mention>(array.length())
        for (i in 0 until array.length()) {
            val obj = array[i] as JSONObject
            val ids = if (obj.has("ids")) obj.getString("ids").split(",") else null
            val screenName = if (obj.has("screenName")) obj.getString("screenName") else null
            val userId = if (obj.has("userId")) obj.getString("userId") else null
            val mention = Mention(screenName = screenName, userId = userId, userIds = ids)
            list.add(mention)
        }
        return list
    }

    override fun encode(value: List<Mention>): String {
        val objects = value.map {
            val jsonObject = JSONObject()
            jsonObject.put("screenName", it.screenName)
            jsonObject.put("userId", it.userId)
            if (it.userIds != null) {
                val ids = TextUtils.join(",", it.userIds)
                jsonObject.put("ids", ids)
            }
            jsonObject
        }
        val json = JSONObject()
        json.put("list", objects)
        return json.toString()
    }

}



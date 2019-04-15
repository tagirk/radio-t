package su.tagir.apps.radiot.model.entries

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.google.gson.annotations.SerializedName
import java.util.*

data class Token(@SerializedName("access_token") val token: String,
                 @SerializedName("token_type") val type: String,
                 @SerializedName("scope") val scope: String? = null,
                 @SerializedName("expires_in") val createdAt: Long? = null)

data class GitterMessage(
        @SerializedName("id") val id: String,
        @SerializedName("text") val text: String? = null,
        @SerializedName("html") val html: String? = null,
        @SerializedName("sent") val sent: Date? = null,
        @SerializedName("editedAt") val editedAt: Date? = null,
        @SerializedName("fromUser") val fromUser: User? = null,
        @SerializedName("unread") val unread: Boolean = false,
        @SerializedName("readBy") val readBy: Int? = null,
        @SerializedName("urls") val urls: List<Url>? = null,
        @SerializedName("mentions") val mentions: List<Mention>? = null) {

    fun toMessage() = Message(id = id,
            text = text,
            html = html,
            sent = sent,
            editedAt = editedAt,
            fromUserId = fromUser?.id,
            unread = unread,
            readBy = readBy,
            urls = urls,
            mentions = mentions)
}

data class Event(
        @SerializedName("id") val id: String,
        @SerializedName("text") val text: String,
        @SerializedName("html") val html: String,
        @SerializedName("sent") val sent: String,
        @SerializedName("editedAt") val editedAt: String,
        @SerializedName("access_token") val meta: String?,
        @SerializedName("payload") val payload: String?,
        @SerializedName("fromUser") val fromUser: User)


@Entity(tableName = User.TABLE)
data class User(
        @PrimaryKey(autoGenerate = false)
        @SerializedName("id") val id: String,
        @SerializedName("username") val username: String? = null,
        @SerializedName("avatarUrl") val avatarUrl: String? = null,
        @SerializedName("avatarUrlSmall") val avatarUrlSmall: String,
        @SerializedName("displayName") val displayName: String? = null,
        @SerializedName("url") val url: String? = null,
        @SerializedName("avatarUrlMedium") val avatarUrlMedium: String? = null) {

    companion object {
        const val TABLE = "chat_user"
        const val ID = "id"
    }
}


data class Url(val url: String?)

data class Mention(val screenName: String?, val userId: String?, val userIds: List<String>?)

@Entity(tableName = Message.TABLE)
data class Message(
        @PrimaryKey(autoGenerate = false)
        val id: String,
        val text: String? = null,
        val html: String? = null,
        val sent: Date? = null,
        val editedAt: Date? = null,
        val fromUserId: String? = null,
        val unread: Boolean = false,
        val readBy: Int? = null,
        val urls: List<Url>? = null,
        val mentions: List<Mention>? = null) {

    companion object {
        const val TABLE = "message"
        const val ID = "id"
        const val FROM_USER = "fromUserId"
        const val SENT = "sent"
    }
}

class MessageFull {

    @Embedded
    var message: Message? = null

    @Relation(parentColumn = Message.FROM_USER, entityColumn = User.ID)
    var user: List<User>? = null


}

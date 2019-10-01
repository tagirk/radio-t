package su.tagir.apps.radiot.model.entries

import com.google.gson.annotations.SerializedName
import su.tagir.apps.radiot.model.entities.MessageQueries
import su.tagir.apps.radiot.model.entities.UserQueries
import java.util.*

data class Token(@SerializedName("access_token") val token: String,
                 @SerializedName("token_type") val type: String,
                 @SerializedName("scope") val scope: String? = null,
                 @SerializedName("expires_in") val expiresIn: Long? = null,
                 @SerializedName("refresh_token") val refreshToken: String? = null)

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


data class User(
        @SerializedName("id") val id: String,
        @SerializedName("username") val username: String? = null,
        @SerializedName("avatarUrl") val avatarUrl: String? = null,
        @SerializedName("avatarUrlSmall") val avatarUrlSmall: String? = null,
        @SerializedName("displayName") val displayName: String? = null,
        @SerializedName("url") val url: String? = null,
        @SerializedName("avatarUrlMedium") val avatarUrlMedium: String? = null) {
}

data class Url(val url: String?)

data class Mention(val screenName: String?, val userId: String?, val userIds: List<String>?)

data class Message(
        val id: String,
        val text: String? = null,
        val html: String? = null,
        val sent: Date? = null,
        val editedAt: Date? = null,
        val fromUserId: String? = null,
        val unread: Boolean = false,
        val readBy: Int? = null,
        val urls: List<Url>? = null,
        val mentions: List<Mention>? = null)

data class MessageFull(val message: Message, val user: User?)

fun Message.insert(messageQueries: MessageQueries) {
    messageQueries.insert(id, text, html, sent, editedAt, fromUserId, unread, readBy, urls, mentions)
}

val messageMapper: (id: String,
                    text: String?,
                    html: String?,
                    sent: Date?,
                    editedAt: Date?,
                    fromUserId: String?,
                    unread: Boolean,
                    readBy: Int?,
                    urls: List<Url>?,
                    mentions: List<Mention>?,
                    userId: String?,
                    userName: String?,
                    displayName: String?,
                    avatarUrl: String?,
                    url: String?) -> MessageFull
    get() = { id,
              text,
              html,
              sent,
              editedAt,
              fromUserId,
              unread,
              readBy,
              urls,
              mentions,
              userId: String?,
              userName: String?,
              displayName: String?,
              avatarUrl: String?,
              url: String? ->
        val message = Message(id, text, html, sent, editedAt, fromUserId, unread, readBy, urls, mentions)
        val user = userId?.let {User(userId, userName, null, avatarUrl, displayName, url, null)}
        MessageFull(message, user)
    }

fun User.insert(userQueries: UserQueries) {
    userQueries.insert(id, username, avatarUrl, avatarUrlSmall, displayName, url, avatarUrlMedium)
}
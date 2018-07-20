package su.tagir.apps.radiot.model.entries

import android.arch.persistence.room.Ignore
import com.google.gson.annotations.SerializedName
import io.mironov.smuggler.AutoParcelable
import java.util.*


data class CommentsTree(
        @SerializedName("comments")
        val comments: List<Node>,

        @SerializedName("info")
        val info: PostInfo? = null
)

data class Node(
        @SerializedName("comment")
        val comment: Comment,

        @SerializedName("replies")
        val replies: List<Node>? = null,

        val level: Int = 1,

        val expanded: Boolean = false
)

data class Comment @Ignore constructor(

        @SerializedName("id")
        val id: String,

        @SerializedName("pid")
        val pid: String? = null,

        @SerializedName("text")
        val text: String? = null,

        @SerializedName("orig")
        val orig: String? = null,

        @SerializedName("user")
        val user: RemarkUser? = null,

        @SerializedName("locator")
        val locator: Locator? = null,

        @SerializedName("score")
        val score: Int? = null,

        @SerializedName("votes")
        val votes: Map<String, Boolean>? = null,

        @SerializedName("time")
        val time: Date? = null,

        @SerializedName("edit")
        val edit: Edit? = null,

        @SerializedName("pin")
        val pin: Boolean? = null,

        @SerializedName("deleted")
        val deleted: Boolean? = null): AutoParcelable


data class RemarkUser(
        @SerializedName("id")
        val id: String,

        @SerializedName("name")
        val name: String = "",

        @SerializedName("picture")
        val picture: String = "",

        @SerializedName("admin")
        val admin: Boolean = false,

        @SerializedName("verified")
        val verified: Boolean = false): AutoParcelable


data class Locator(
        @SerializedName("site")
        val site: String? = null,

        @SerializedName("url")
        val url: String = ""): AutoParcelable

data class Edit(
        @SerializedName("time")
        val time: Date,

        @SerializedName("summary")
        val summary: String = ""):AutoParcelable

data class PostInfo(
        @SerializedName("url") val url: String = "",
        @SerializedName("count") val count: Int = 0,
        @SerializedName("read_only") val readOnly: Boolean?,
        @SerializedName("first_time") var firstTS: Date?,
        @SerializedName("last_time") val lastTS: Date?)




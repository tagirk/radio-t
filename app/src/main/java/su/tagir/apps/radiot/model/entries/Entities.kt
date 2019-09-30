package su.tagir.apps.radiot.model.entries

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


data class Progress(var duration: Long = 0L,
                    var progress: Long = 0L) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readLong())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(duration)
        parcel.writeLong(progress)
    }

    fun readFromParcel(`in`: Parcel) {
        duration = `in`.readLong()
        progress = `in`.readLong()
    }


    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Progress> {
        override fun createFromParcel(parcel: Parcel): Progress {
            return Progress(parcel)
        }

        override fun newArray(size: Int): Array<Progress?> {
            return arrayOfNulls(size)
        }
    }
}

@Entity(tableName = "article")
data class Article(
        @PrimaryKey(autoGenerate = false)
        @SerializedName("slug") val slug: String,
        @SerializedName("title") val title: String? = null,
        @SerializedName("content") val content: String? = null,
        @SerializedName("snippet") val snippet: String? = null,
        @SerializedName("pic") val image: String? = null,
        @SerializedName("link") val link: String? = null,
        @SerializedName("author") val author: String? = null,
        @SerializedName("ts") val date: Date? = null,
        @SerializedName("ats") val addedDate: Date? = null,
        @SerializedName("active") val active: Boolean = false,
        @SerializedName("geek") val geek: Boolean = false,
        @SerializedName("domain") val domain: String? = null,
        @SerializedName("comments") val comments: Int = 0,
        @SerializedName("likes") val likes: Int = 0,
        @SerializedName("del") val deleted: Boolean = false,
        @SerializedName("archived") val archived: Boolean = false
)

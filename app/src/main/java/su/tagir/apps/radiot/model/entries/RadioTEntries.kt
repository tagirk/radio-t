package su.tagir.apps.radiot.model.entries

import com.google.gson.annotations.SerializedName
import java.util.*

data class RTEntry(
        @SerializedName("url") val url: String,
        @SerializedName("title") val title: String,
        @SerializedName("date") val date: Date,
        @SerializedName("categories") val categories: List<String>? = null,
        @SerializedName("image") val image: String? = null,
        @SerializedName("file_name") val fileName: String? = null,
        @SerializedName("body") val body: String? = null,
        @SerializedName("show_notes") val showNotes: String? = null,
        @SerializedName("audio_url") val audioUrl: String? = null,
        @SerializedName("time_labels") val timeLabels: List<RTTimeLabel>? = null)

data class RTTimeLabel(
        @SerializedName("topic") val topic: String,
        @SerializedName("time") val time: Date?,
        @SerializedName("duration") val duration: Long?)




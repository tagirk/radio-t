package su.tagir.apps.radiot.model.entries

import java.util.*

data class TimeLabel(
        val topic: String,
        val time: Long?,
        val duration: Long?,
        val podcastTime: Date?)

val timeLabelMapper: (topic: String,
                      time: Long?,
                      duration: Long?,
                      podcastTime: Date?) -> TimeLabel

    get() = { topic, time, duration, podcastTime -> TimeLabel(topic, time, duration, podcastTime) }
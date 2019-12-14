package su.tagir.apps.radiot.utils

import android.annotation.SuppressLint
import android.view.View
import timber.log.Timber
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun View.visibleGone(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.visibleInvisible(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.INVISIBLE
}

fun Date.timeOfDay(): Long {
    val c = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))
    c.time = this
    c.set(0, 0, 1)
    val h = c.get(Calendar.HOUR_OF_DAY)
    val m = c.get(Calendar.MINUTE)
    val s = c.get(Calendar.SECOND)
    val ms = c.get(Calendar.MILLISECOND)
    return h * 3600_000L + m * 60_000L + s * 1000L + ms
}

fun Date.startOfDay(): Date{
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))
    calendar.time = this
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

fun Date.longDateTimeFormat(): String {
    val format = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    return format.format(this)
}

fun Date.longDateFormat(): String {
    val format = SimpleDateFormat.getDateInstance(DateFormat.LONG)
    format.timeZone = TimeZone.getTimeZone("America/Chicago")
    return format.format(this)
}

fun Date.shortDateFormat(): String {
    val format = SimpleDateFormat.getDateInstance(DateFormat.SHORT)
    format.timeZone = TimeZone.getTimeZone("America/Chicago")
    return format.format(this)
}

fun Long?.convertMillis(): String {
    return (this?.div(1000)).convertSeconds()
}

@SuppressLint("SimpleDateFormat")
fun String.parseDate(pattern: String):Date{
    val format = SimpleDateFormat(pattern, Locale.ENGLISH)
    return try {
        format.parse(this)
    }catch (e:Exception){
        Timber.e(e)
        Date()
    }
}

fun Long?.convertSeconds(): String {
    if (this == null || this == 0L) {
        return "00:00"
    }
    val h = this / 3600
    var s = this % 3600
    val m = s / 60
    s %= 60
    if (h > 0) {
        return String.format("%d:%02d:%02d", h, m, s)
    }
    return String.format("%02d:%02d", m, s)
}
fun Int?.convertSeconds(): String {
    if (this == null || this == 0) {
        return "00:00"
    }
    val h = this / 3600
    var s = this % 3600
    val m = s / 60
    s %= 60
    if (h > 0) {
        return String.format("%d:%02d:%02d", h, m, s)
    }
    return String.format("%02d:%02d", m, s)
}





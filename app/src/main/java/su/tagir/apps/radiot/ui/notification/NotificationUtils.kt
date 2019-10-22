package su.tagir.apps.radiot.ui.notification


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.PodcastStateService
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.MainActivity
import su.tagir.apps.radiot.utils.longDateFormat
import java.util.*

const val SERVICE_CHANNEL = "AudioService"
const val NOTIFICATION_CHANNEL = "Notification"

@RequiresApi(Build.VERSION_CODES.O)
fun createNotificationsChannels(context: Context) {
    val serviceChannel = createChannel(SERVICE_CHANNEL, "AudioService", "AudioService")
    val notificationChannel = createChannel(NOTIFICATION_CHANNEL, "Notification", "Notification")
    val notificationManger = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManger.createNotificationChannels(listOf(serviceChannel, notificationChannel))
}

@RequiresApi(Build.VERSION_CODES.O)
private fun createChannel(id: String, name: String, description: String): NotificationChannel {
    val importance = NotificationManager.IMPORTANCE_LOW
    val channel = NotificationChannel(id, name, importance)
    channel.description = description
    channel.enableLights(true)
    channel.lightColor = Color.BLUE
    channel.enableVibration(true)
    return channel
}

fun notify(notification: Notification, id: Int, context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(id, notification)
}

@Suppress("DEPRECATION")
fun createStreamNotification(context: Context): Notification {
    val intent = Intent(context, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    val pIntent = PendingIntent.getActivity(context, 12, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    val notificationBuilder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
            } else {
                NotificationCompat.Builder(context)
            }

    return notificationBuilder
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(NotificationCompat.BigTextStyle())
            .setContentIntent(pIntent)
            .setContentTitle("Напоминание о прямом эфире")
            .setContentText("Не пропустите сегодня в 23:00 мск.")
            .setAutoCancel(true)
            .setCategory(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Notification.CATEGORY_REMINDER else Notification.CATEGORY_EVENT)
            .build()

}

@Suppress("DEPRECATION")
fun createMediaNotification(entry: Entry?, paused: Boolean, icon: Bitmap? = null, context: Context): Notification {

    val intent = Intent(context, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    val pIntent = PendingIntent.getActivity(context, 12, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    val notificationBuilder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationCompat.Builder(context, SERVICE_CHANNEL)
            } else {
                @Suppress("DEPRECATION")
                NotificationCompat.Builder(context)
            }


    if (!paused) {
        val pause = Intent(context, PodcastStateService::class.java)
        pause.action = PodcastStateService.ACTION_PAUSE
        val pausePIntent = PendingIntent.getService(context, 42, pause, PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder.addAction(R.drawable.ic_pause_black_png, "Pause", pausePIntent)
    } else {
        val play = Intent(context, PodcastStateService::class.java)
        play.action = PodcastStateService.ACTION_RESUME
        val playPIntent = PendingIntent.getService(context, 41, play, PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder.addAction(R.drawable.ic_play_black_png, "Play", playPIntent)
    }

    val stop = Intent(context, PodcastStateService::class.java)
    stop.action = PodcastStateService.ACTION_STOP
    val stopPIntent = PendingIntent.getService(context, 42, stop, PendingIntent.FLAG_UPDATE_CURRENT)
    notificationBuilder.addAction(R.drawable.ic_clear_black_png, "Stop", stopPIntent)

    return notificationBuilder
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pIntent)
            .setContentTitle(entry?.title)
            .apply {
                icon?.let { setLargeIcon(icon) }
            }
            .setContentText(if (entry?.date?.time ?: 0 > 0) entry?.date?.longDateFormat() else Date().longDateFormat())
            .build()
}
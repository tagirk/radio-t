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
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.image.ImageConfig
import su.tagir.apps.radiot.image.ImageLoader
import su.tagir.apps.radiot.image.Target
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
    val importance = android.app.NotificationManager.IMPORTANCE_LOW
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
                Notification.Builder(context, NOTIFICATION_CHANNEL)
            } else {
                Notification.Builder(context)
            }

    return notificationBuilder
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(Notification.BigTextStyle())
            .setContentIntent(pIntent)
            .setContentTitle("Напоминание о прямом эфире")
            .setContentText("Не пропустите сегодня в 23:00 мск.")
            .setAutoCancel(true)
            .setCategory(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Notification.CATEGORY_REMINDER else Notification.CATEGORY_EVENT)
            .build()

}

@Suppress("DEPRECATION")
fun createMediaNotification(entry: Entry?, paused: Boolean, context: Context): Notification {

    val intent = Intent(context, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    val pIntent = PendingIntent.getActivity(context, 12, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    val notificationBuilder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(context, SERVICE_CHANNEL)
            } else {
                @Suppress("DEPRECATION")
                Notification.Builder(context)

            }

    entry?.image?.let{url ->

        val target = object : Target{
            override fun onLoaded(bitmap: Bitmap) {
                notificationBuilder.setLargeIcon(bitmap)
            }

        }
        val config = ImageConfig(retreiveFromCacheOnly = true)
        ImageLoader.load(url, target, config)

//        val bitmap = GlideApp.with(context.applicationContext)
//                .asBitmap()
//                .load(entry.image)
//                .onlyRetrieveFromCache(true)
//                .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
//                .get(1, TimeUnit.SECONDS)
//
//        notificationBuilder.setLargeIcon(bitmap)
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
            .setStyle(Notification.MediaStyle().setShowActionsInCompactView(0))
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setContentIntent(pIntent)
            .setContentTitle(entry?.title)
            .setContentText(if (entry?.date?.time ?: 0 > 0) entry?.date?.longDateFormat() else Date().longDateFormat())
            .build()
}
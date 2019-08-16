package edu.phystech.iag.kaiumov.anyassecret.notification

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import edu.phystech.iag.kaiumov.anyassecret.R
import java.util.*
import kotlin.random.Random


// https://developer.android.com/training/scheduling/alarms

object AlarmStarter {
    
    class AlarmNotificationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            showNotification(context)
        }
    }
    
    fun start(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

        val calendar: Calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 10)
        }
        val now = Calendar.getInstance().timeInMillis
        if (calendar.timeInMillis - now <= 0) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    // Unique identifier for notification
    private const val NOTIFICATION_ID = 101
    // This is the Notification Channel ID
    private const val NOTIFICATION_CHANNEL_ID = "channel_id"
    //User visible Channel Name
    private const val CHANNEL_NAME = "Compliments from Yasha"
    // Importance applicable to all the notifications in this Channel
    private const val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
    // Pattern is in milliseconds with the format {delay,play,sleep,play,sleep...}
    private val VIBRATE_PATTERN = longArrayOf(500, 500, 500, 500)

    fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var notificationChannel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
            if (notificationChannel == null) {
                notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, CHANNEL_NAME, IMPORTANCE)
                //Boolean value to set if lights are enabled for Notifications from this Channel
                notificationChannel.enableLights(true)
                //Boolean value to set if vibration are enabled for Notifications from this Channel
                notificationChannel.enableVibration(true)
                //Sets the color of Notification Light
                notificationChannel.lightColor = Color.GREEN

                notificationChannel.vibrationPattern = VIBRATE_PATTERN
                //Sets whether notifications from these Channel should be visible on Lockscreen or not
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }

        val strings = context.resources.getStringArray(R.array.compliment)
        val text = strings[Random.nextInt(strings.size)]
        // Notification Channel ID passed as a parameter here will be ignored for all the Android versions below 8.0
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        builder.priority = NotificationCompat.PRIORITY_HIGH
        builder
            .setAutoCancel(true)
            .setVibrate(VIBRATE_PATTERN)
            .setContentTitle(context.resources.getString(R.string.compliment))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setSmallIcon(R.drawable.ic_mood)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build())
    }
}
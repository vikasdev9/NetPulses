package com.example.netpulse.thermal.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.netpulse.MainActivity
import com.example.netpulse.R
import com.example.netpulse.data.datastore.UserPreferences
import kotlinx.coroutines.flow.first

class ThermalNotificationHelper(private val context: Context, private val userPreferences: UserPreferences) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "thermal_monitor_channel"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Thermal Monitor"
            val descriptionText = "Notifications for high device temperature"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    suspend fun showHighNotification(temp: Float) {
        showNotification(
            "High Temperature Detected",
            "Your device temperature is rising: ${"%.1f".format(temp)}°C. Consider closing heavy apps.",
            1001
        )
    }

    suspend fun showCriticalNotification(temp: Float) {
        showNotification(
            "Critical Temperature Warning!",
            "Extreme heat detected: ${"%.1f".format(temp)}°C. Please disconnect charger and let it cool.",
            1002
        )
    }

    private suspend fun showNotification(title: String, message: String, id: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NAVIGATE_TO", "thermal")
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val soundEnabled = userPreferences.thermalNotifySound.first()
        val vibrationEnabled = userPreferences.thermalNotifyVibration.first()
        val ringtoneUri = userPreferences.thermalNotifyRingtone.first()
        val priorityStr = userPreferences.thermalNotifyPriority.first()

        val priority = when (priorityStr) {
            "Low" -> NotificationCompat.PRIORITY_LOW
            "High" -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.netpulsesicon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (soundEnabled) {
            if (ringtoneUri.isNotEmpty()) {
                builder.setSound(Uri.parse(ringtoneUri))
            } else {
                builder.setDefaults(NotificationCompat.DEFAULT_SOUND)
            }
        }

        if (vibrationEnabled) {
            builder.setVibrate(longArrayOf(1000, 1000, 1000))
        }

        notificationManager.notify(id, builder.build())
    }
}

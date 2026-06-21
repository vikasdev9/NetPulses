package com.example.netpulse.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.netpulse.MainActivity
import com.example.netpulse.R
import java.util.*

object NotificationHelper {
    const val CHANNEL_TEST_RESULTS = "channel_test_results"
    const val CHANNEL_ALERTS = "channel_alerts"
    const val CHANNEL_DAILY_SUMMARY = "channel_daily_summary"
    const val CHANNEL_WIDGET = "channel_widget"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val testResults = NotificationChannel(
                CHANNEL_TEST_RESULTS,
                "Speed Test Results",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when a speed test completes"
            }

            val alerts = NotificationChannel(
                CHANNEL_ALERTS,
                "Network Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for speed drops or slow internet"
            }

            val dailySummary = NotificationChannel(
                CHANNEL_DAILY_SUMMARY,
                "Daily Summary",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Daily summary of your network performance"
            }

            val widget = NotificationChannel(
                CHANNEL_WIDGET,
                "Widget Tests",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background tests from the home screen widget"
            }

            manager.createNotificationChannels(listOf(testResults, alerts, dailySummary, widget))
        }
    }

    fun showTestCompleteNotification(context: Context, download: Double, upload: Double, ping: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("START_TEST", true)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, CHANNEL_TEST_RESULTS)
            .setSmallIcon(R.drawable.ic_bolt)
            .setContentTitle("Speed Test Complete ⚡")
            .setContentText("↓ ${"%.1f".format(download)} Mbps  ↑ ${"%.1f".format(upload)} Mbps  Ping ${ping}ms")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_bolt, "Test Again", pendingIntent)
            .setContentIntent(pendingIntent)
            .build()

        notify(context, 1001, notification)
    }

    fun showSpeedDropAlert(context: Context, current: Double, avg: Double) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_bolt)
            .setContentTitle("Speed Drop Detected 📉")
            .setContentText("Your speed dropped to ${"%.1f".format(current)} Mbps. Average is ${"%.1f".format(avg)} Mbps.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_bolt, "Run Test", pendingIntent)
            .setContentIntent(pendingIntent)
            .build()

        notify(context, 1002, notification)
    }

    fun showSlowInternetWarning(context: Context, speed: Double) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_bolt)
            .setContentTitle("Slow Internet Detected 🐢")
            .setContentText("Your speed is only ${"%.1f".format(speed)} Mbps. This may affect video calls and streaming.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_bolt, "View Results", pendingIntent)
            .setContentIntent(pendingIntent)
            .build()

        notify(context, 1003, notification)
    }

    fun showDailySummary(context: Context, tests: Int, best: Double, data: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("NAVIGATE_TO", "analytics")
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_SUMMARY)
            .setSmallIcon(R.drawable.ic_bolt)
            .setContentTitle("Your Daily Network Summary 📊")
            .setContentText("Today: $tests tests run. Best: ${"%.1f".format(best)} Mbps. Data used: $data")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notify(context, 1004, notification)
    }

    fun showWidgetTestComplete(context: Context, download: Double, upload: Double) {
        val notification = NotificationCompat.Builder(context, CHANNEL_WIDGET)
            .setSmallIcon(R.drawable.ic_bolt)
            .setContentTitle("Background Test Complete")
            .setContentText("↓ ${"%.1f".format(download)} Mbps  ↑ ${"%.1f".format(upload)} Mbps")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setTimeoutAfter(5000)
            .build()

        notify(context, 1005, notification)
    }

    private fun notify(context: Context, id: Int, notification: android.app.Notification) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < 33) {
            NotificationManagerCompat.from(context).notify(id, notification)
        }
    }

    fun cancelAll(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }
}

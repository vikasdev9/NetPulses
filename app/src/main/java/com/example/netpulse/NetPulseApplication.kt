package com.example.netpulse

import android.app.Application
import android.content.Context
import com.example.netpulse.data.NetPulseDatabase
import com.example.netpulse.insights.dailyreport.DailyReportWorker
import com.example.netpulse.insights.wifistability.WifiStabilityWorker
import com.example.netpulse.utils.LocaleUtils
import com.example.netpulse.utils.NotificationHelper
import androidx.work.*
import java.util.concurrent.TimeUnit

class NetPulseApplication : Application() {
    val database by lazy { NetPulseDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        scheduleDailyReport()
        scheduleWifiMonitoring()
    }

    private fun scheduleWifiMonitoring() {
        val workRequest = PeriodicWorkRequestBuilder<WifiStabilityWorker>(15, TimeUnit.MINUTES)
            .addTag("wifi_monitoring")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "wifi_monitoring",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun scheduleDailyReport() {
        val workRequest = PeriodicWorkRequestBuilder<DailyReportWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateDelayToMidnight(), TimeUnit.MILLISECONDS)
            .addTag("daily_report")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_report",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun calculateDelayToMidnight(): Long {
        val calendar = java.util.Calendar.getInstance()
        val now = calendar.timeInMillis
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        return calendar.timeInMillis - now
    }

    override fun attachBaseContext(base: Context) {
        val lang = LocaleUtils.getSavedLanguage(base)
        if (lang.isNotEmpty()) {
            super.attachBaseContext(LocaleUtils.setLocale(base, lang))
        } else {
            super.attachBaseContext(base)
        }
    }
}

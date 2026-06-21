package com.example.netpulse.service

import android.content.Context
import androidx.work.*
import com.example.netpulse.NetPulseApplication
import com.example.netpulse.data.analytics.DataUsageHelper
import com.example.netpulse.utils.NotificationHelper
import kotlinx.coroutines.flow.first
import java.util.*
import java.util.concurrent.TimeUnit

class DailySummaryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): ListenableWorker.Result {
        val app = applicationContext as NetPulseApplication
        val dao = app.database.speedResultDao()
        val userPrefs = com.example.netpulse.data.datastore.UserPreferences(applicationContext)

        val notificationsEnabled = userPrefs.notificationsEnabled.first()
        if (!notificationsEnabled) return ListenableWorker.Result.success()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfDay = calendar.timeInMillis

        val todayResults = dao.getResultsAfter(startOfDay).first()
        if (todayResults.isEmpty()) return ListenableWorker.Result.success()

        val bestDownload = todayResults.maxOfOrNull { it.downloadMbps } ?: 0.0
        val testCount = todayResults.size
        
        val dataUsageHelper = DataUsageHelper(applicationContext)
        val wifiData = dataUsageHelper.getTodayWifiData()
        val mobileData = dataUsageHelper.getTodayMobileData()
        val totalDataFormatted = com.example.netpulse.data.analytics.formatBytes(wifiData.totalBytes + mobileData.totalBytes)

        NotificationHelper.showDailySummary(
            context = applicationContext,
            tests = testCount,
            best = bestDownload,
            data = totalDataFormatted
        )

        return ListenableWorker.Result.success()
    }

    companion object {
        private const val WORK_NAME = "daily_summary_work"

        fun schedule(context: Context) {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 20) // 8 PM
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)

            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val delay = calendar.timeInMillis - System.currentTimeMillis()

            val request = PeriodicWorkRequestBuilder<DailySummaryWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}

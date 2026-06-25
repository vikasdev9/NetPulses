package com.example.netpulse.insights.usage

import android.content.Context
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

class DailyResetWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        UsageRepository(applicationContext).resetToday()
        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val delay = calendar.timeInMillis - System.currentTimeMillis()

            val request = PeriodicWorkRequestBuilder<DailyResetWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "daily_usage_reset",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}

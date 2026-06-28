package com.example.netpulse.thermal.worker

import android.content.Context
import androidx.work.*
import com.example.netpulse.data.datastore.UserPreferences
import com.example.netpulse.thermal.repository.ThermalRepository
import com.example.netpulse.thermal.notification.ThermalNotificationHelper
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class ThermalWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val userPreferences = UserPreferences(context)
    private val repository = ThermalRepository(context, userPreferences)
    private val notificationHelper = ThermalNotificationHelper(context, userPreferences)

    override suspend fun doWork(): Result {
        val enabled = userPreferences.thermalMonitorEnabled.first()
        if (!enabled) return Result.success()

        val data = repository.getLatestThermalData().copy(source = "Background")
        
        // 1. Check thresholds
        val highThreshold = userPreferences.thermalHighThreshold.first()
        val criticalThreshold = userPreferences.thermalCriticalThreshold.first()
        
        var notificationFired = false
        
        if (data.temperature >= criticalThreshold) {
            if (userPreferences.thermalNotifyCritical.first()) {
                notificationHelper.showCriticalNotification(data.temperature)
                notificationFired = true
            }
        } else if (data.temperature >= highThreshold) {
            if (userPreferences.thermalNotifyHigh.first()) {
                notificationHelper.showHighNotification(data.temperature)
                notificationFired = true
            }
        }

        // 2. Save to database
        repository.saveThermalData(data, notificationFired)

        // 3. Auto delete old history if enabled
        if (userPreferences.thermalAutoDeleteOld.first()) {
            repository.deleteOldHistory()
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "ThermalMonitorWorker"

        fun schedule(context: Context, intervalMinutes: Long) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val request = PeriodicWorkRequestBuilder<ThermalWorker>(intervalMinutes, TimeUnit.MINUTES)
                .setConstraints(constraints)
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

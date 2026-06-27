package com.example.netpulse.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.*
import com.example.netpulse.data.datastore.UserPreferences
import com.example.netpulse.widget.WidgetDataStore
import com.example.netpulse.widget.WidgetRefreshManager
import com.example.netpulse.utils.NetworkStateManager
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class WidgetUpdateWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userPreferences = UserPreferences(context)
        if (!userPreferences.widgetsEnabled.first()) return Result.success()

        // Fetch latest network info for accuracy
        try {
            val ispInfo = com.example.netpulse.utils.IspInfoHelper.fetchDetailedIspInfo()
            val networkStateManager = com.example.netpulse.utils.NetworkStateManager(context)
            networkStateManager.startObserving()
            val networkState = networkStateManager.networkState.value
            
            WidgetDataStore.updateNetworkInfo(
                context = context,
                wifiName = if (networkState.networkType == "WiFi") "Connected" else networkState.networkType,
                signal = 4, // Placeholder
                publicIp = ispInfo.ip,
                localIp = "—",
                vpn = ispInfo.org.contains("VPN", ignoreCase = true),
                dns = "—",
                battery = 100, // Placeholder
                usage = "—"
            )
            networkStateManager.stopObserving()
        } catch (e: Exception) {
            // Ignore background errors
        }
        
        WidgetRefreshManager.refreshAllWidgets(context)

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "widget_update_worker"

        fun schedule(context: Context, intervalMinutes: Long) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                intervalMinutes, TimeUnit.MINUTES
            )
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

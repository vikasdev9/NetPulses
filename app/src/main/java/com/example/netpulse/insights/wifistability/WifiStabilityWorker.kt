package com.example.netpulse.insights.wifistability

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import androidx.work.*
import com.example.netpulse.data.network.SpeedTestEngine
import java.util.concurrent.TimeUnit

class WifiStabilityWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repository = WifiStabilityRepository(applicationContext)
        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifi = applicationContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        
        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)
        val connected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        
        val type = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Mobile"
            else -> "None"
        }
        
        val rssi = if (type == "WiFi") wifi.connectionInfo.rssi else -100
        val pingResult = if (connected) SpeedTestEngine.measurePing() else Pair(-1.0, -1.0)
        
        repository.recordStability(pingResult.first.toInt(), rssi, type, connected)
        
        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WifiStabilityWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "wifi_stability",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}

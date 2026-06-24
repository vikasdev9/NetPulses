package com.example.netpulse.insights.wifistability

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.netpulse.NetPulseApplication
import com.example.netpulse.data.network.SpeedTestEngine
import kotlinx.coroutines.flow.first

class WifiStabilityWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as NetPulseApplication
        val dao = app.database.wifiStabilityDao()
        
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val (ping, _) = SpeedTestEngine.measurePing()
        val rssi = wifiManager.connectionInfo.rssi
        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)
        
        val type = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Mobile"
            else -> "None"
        }
        
        val isConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        
        val entity = WifiStabilityEntity(
            timestamp = System.currentTimeMillis(),
            pingMs = ping,
            rssi = rssi,
            networkType = type,
            isConnected = isConnected
        )
        
        dao.insert(entity)
        
        return Result.success()
    }
}

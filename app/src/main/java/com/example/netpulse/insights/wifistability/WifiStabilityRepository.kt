package com.example.netpulse.insights.wifistability

import android.content.Context
import com.example.netpulse.NetPulseApplication
import com.example.netpulse.data.analytics.WifiStabilityEntity
import com.example.netpulse.ui.viewmodel.StabilityMetrics
import kotlinx.coroutines.flow.first
import java.util.*

class WifiStabilityRepository(private val context: Context) {
    private val dao = (context.applicationContext as NetPulseApplication).database.wifiStabilityDao()

    suspend fun recordStability(pingMs: Int, rssi: Int, type: String, connected: Boolean) {
        dao.insert(WifiStabilityEntity(0, System.currentTimeMillis(), pingMs, rssi, type, connected))
    }

    suspend fun getMetrics(): StabilityMetrics {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }.timeInMillis
        
        val results = dao.getForToday(todayStart).first()
        if (results.isEmpty()) return StabilityMetrics()

        val connectedCount = results.count { it.isConnected }
        val uptime = (connectedCount.toFloat() / results.size * 100).toInt()
        
        val avgRssi = results.map { it.rssi }.average()
        val signalLabel = when {
            avgRssi > -50 -> "Excellent"
            avgRssi > -60 -> "Good"
            avgRssi > -70 -> "Fair"
            else -> "Weak"
        }

        val disconnections = results.zipWithNext().count { it.first.isConnected && !it.second.isConnected }
        
        val pings = results.filter { it.isConnected && it.pingMs > 0 }.map { it.pingMs.toDouble() }
        val stdDev = if (pings.size > 1) {
            val avg = pings.average()
            Math.sqrt(pings.map { Math.pow(it - avg, 2.0) }.average()).toFloat()
        } else 0f

        return StabilityMetrics(
            uptimePercentage = uptime,
            signalLabel = signalLabel,
            disconnectionCount = disconnections,
            pingStability = stdDev,
            liveSignalStrength = if (results.isNotEmpty()) WifiUtils.calculateSignalLevel(results.last().rssi) else 0
        )
    }
}

object WifiUtils {
    fun calculateSignalLevel(rssi: Int): Int {
        return ((rssi + 100).coerceIn(0, 100)) // Simplified mapping
    }
}

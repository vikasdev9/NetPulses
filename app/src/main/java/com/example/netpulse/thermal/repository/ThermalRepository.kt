package com.example.netpulse.thermal.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import com.example.netpulse.data.NetPulseDatabase
import com.example.netpulse.data.datastore.UserPreferences
import com.example.netpulse.thermal.database.ThermalEntity
import com.example.netpulse.thermal.model.ThermalData
import com.example.netpulse.thermal.model.ThermalStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.*

class ThermalRepository(private val context: Context, private val userPreferences: UserPreferences) {

    private val thermalDao = NetPulseDatabase.getDatabase(context).thermalDao()
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    fun getLatestThermalData(): ThermalData {
        val batteryStatus: Intent? = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        
        val temp = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)?.let { it / 10f } ?: 0f
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (scale > 0) (level * 100 / scale.toFloat()).toInt() else 0
        
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val thermalApiStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (powerManager.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE -> "Normal"
                PowerManager.THERMAL_STATUS_LIGHT -> "Light"
                PowerManager.THERMAL_STATUS_MODERATE -> "Moderate"
                PowerManager.THERMAL_STATUS_SEVERE -> "Severe"
                PowerManager.THERMAL_STATUS_CRITICAL -> "Critical"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency"
                PowerManager.THERMAL_STATUS_SHUTDOWN -> "Shutdown"
                else -> "Unknown"
            }
        } else "Not Available"

        return ThermalData(
            temperature = temp,
            status = classifyTemperature(temp),
            isCharging = isCharging,
            batteryLevel = batteryPct,
            thermalApiStatus = thermalApiStatus
        )
    }

    private fun classifyTemperature(temp: Float): ThermalStatus {
        return when {
            temp < 35f -> ThermalStatus.COOL
            temp < 40f -> ThermalStatus.NORMAL
            temp < 45f -> ThermalStatus.WARM
            temp < 50f -> ThermalStatus.HOT
            else -> ThermalStatus.CRITICAL
        }
    }

    suspend fun saveThermalData(data: ThermalData, notificationFired: Boolean = false) {
        if (userPreferences.thermalSaveHistory.first()) {
            thermalDao.insert(
                ThermalEntity(
                    temperature = data.temperature,
                    status = data.status.name,
                    batteryLevel = data.batteryLevel,
                    isCharging = data.isCharging,
                    timestamp = data.timestamp,
                    notificationFired = notificationFired,
                    source = data.source
                )
            )
        }
    }

    fun getHistory(): Flow<List<ThermalEntity>> = thermalDao.getAll()

    fun getHistoryForPeriod(days: Int): Flow<List<ThermalEntity>> {
        val startTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return thermalDao.getHistoryFrom(startTime)
    }

    suspend fun getStatsToday(): Map<String, Float?> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        val startOfDay = calendar.timeInMillis
        
        return mapOf(
            "max" to thermalDao.getMaxTempToday(startOfDay),
            "min" to thermalDao.getMinTempToday(startOfDay),
            "avg" to thermalDao.getAvgTempToday(startOfDay)
        )
    }

    suspend fun deleteOldHistory() {
        val days = userPreferences.thermalMaxHistoryDays.first()
        val threshold = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        thermalDao.deleteOldHistory(threshold)
    }
}

package com.example.netpulse.data.lan

import com.example.netpulse.data.lan.db.LanDao
import com.example.netpulse.utils.lan.LanScannerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LanScannerRepository(
    private val scannerManager: LanScannerManager,
    private val lanDao: LanDao
) {
    val isScanning = scannerManager.isScanning
    
    // Combine discovered devices with history to get nicknames, notes, and favorite status
    val discoveredDevices: Flow<List<LanDevice>> = combine(
        scannerManager.discoveredDevices,
        lanDao.getAllHistory()
    ) { discovered, history ->
        val list = discovered.map { device ->
            val h = history.find { it.ipAddress == device.ipAddress }
            if (h != null) {
                device.copy(
                    nickname = h.nickname,
                    notes = h.notes,
                    isFavorite = h.isFavorite,
                    firstSeen = h.firstSeen,
                    deviceType = try { DeviceType.valueOf(h.deviceType) } catch (e: Exception) { device.deviceType },
                    os = h.os
                )
            } else {
                device
            }
        }
        
        // Auto-save discovered devices to history if scanning just finished or found new ones
        if (discovered.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                discovered.forEach { saveDeviceToHistory(it) }
            }
        }
        
        list
    }

    fun startScan() = scannerManager.startScan()
    fun stopScan() = scannerManager.stopScan()
    fun getNetworkInfo() = scannerManager.getNetworkInfo()

    fun getHistory() = lanDao.getAllHistory()

    suspend fun updateNickname(ip: String, nickname: String?) {
        lanDao.updateNickname(ip, nickname)
    }

    suspend fun updateNotes(ip: String, notes: String?) {
        lanDao.updateNotes(ip, notes)
    }

    suspend fun toggleFavorite(ip: String, isFavorite: Boolean) {
        lanDao.updateFavorite(ip, isFavorite)
    }

    suspend fun saveDeviceToHistory(device: LanDevice) {
        lanDao.insertOrUpdate(
            LanDeviceEntity(
                ipAddress = device.ipAddress,
                hostname = device.hostname,
                macAddress = device.macAddress,
                vendor = device.vendor,
                deviceType = device.deviceType.name,
                nickname = device.nickname,
                notes = device.notes,
                isFavorite = device.isFavorite,
                firstSeen = device.firstSeen,
                lastSeen = device.lastSeen,
                os = device.os
            )
        )
    }

    fun calculateNetworkHealth(devices: List<LanDevice>, avgLatency: Long): Int {
        if (devices.isEmpty()) return 0
        var score = 100
        
        // Penalty for high latency
        if (avgLatency > 50) score -= 10
        if (avgLatency > 150) score -= 20
        
        // Penalty for many devices (potential congestion)
        if (devices.size > 20) score -= 5
        if (devices.size > 50) score -= 15
        
        // Penalty for router unreachable (impossible if we found other devices usually, but still)
        val router = devices.find { it.isRouter }
        if (router == null || !router.isOnline) score -= 50
        
        return score.coerceIn(0, 100)
    }

    fun generateInsights(devices: List<LanDevice>, avgLatency: Long): List<String> {
        val insights = mutableListOf<String>()
        if (devices.isEmpty()) return insights
        
        val online = devices.count { it.isOnline }
        if (online > 25) insights.add("High number of active devices ($online).")
        if (avgLatency > 100) insights.add("High network latency detected.")
        
        val router = devices.find { it.isRouter }
        if (router != null && router.latencyMs > 20) insights.add("Router response time is slow.")
        
        if (devices.any { it.isUnknown }) insights.add("Unknown devices detected on network.")
        
        return insights
    }
}

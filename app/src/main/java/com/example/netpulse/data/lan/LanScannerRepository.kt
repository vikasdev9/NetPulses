package com.example.netpulse.data.lan

import com.example.netpulse.data.lan.db.LanDao
import com.example.netpulse.utils.lan.LanScannerManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LanScannerRepository(
    private val scannerManager: LanScannerManager,
    private val lanDao: LanDao
) {
    val discoveredDevices = scannerManager.discoveredDevices
    val isScanning = scannerManager.isScanning

    fun startScan() = scannerManager.startScan()
    fun stopScan() = scannerManager.stopScan()
    fun getNetworkInfo() = scannerManager.getNetworkInfo()

    fun getHistory() = lanDao.getAllHistory()

    suspend fun updateNickname(ip: String, nickname: String?) {
        lanDao.updateNickname(ip, nickname)
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
                lastSeen = device.lastSeen
            )
        )
    }
}

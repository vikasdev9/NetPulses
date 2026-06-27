package com.example.netpulse.data.wifi

import com.example.netpulse.data.wifi.db.WifiDao
import com.example.netpulse.utils.wifi.WifiScannerManager
import kotlinx.coroutines.flow.Flow

class WifiScannerRepository(
    private val scannerManager: WifiScannerManager,
    private val wifiDao: WifiDao
) {
    val scanResults: Flow<List<WifiNetwork>> = scannerManager.scanResults

    val scanState = scannerManager.scanState

    fun startScan() = scannerManager.startScan()

    fun stopScan() = scannerManager.stopScan()

    fun getHistory() = wifiDao.getAllHistory()

    suspend fun toggleFavorite(bssid: String, isFavorite: Boolean) {
        wifiDao.updateFavorite(bssid, isFavorite)
    }

    suspend fun clearHistory() {
        wifiDao.clearHistory()
    }
}

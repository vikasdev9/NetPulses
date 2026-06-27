package com.example.netpulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.netpulse.data.wifi.WifiScannerRepository

class WifiScannerViewModel(
    private val repository: WifiScannerRepository
) : ViewModel() {

    val scanResults = repository.scanResults
    val scanState = repository.scanState

    fun startScan() {
        repository.startScan()
    }

    fun stopScan() {
        repository.stopScan()
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopScan()
    }
}

package com.example.netpulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netpulse.data.lan.LanDevice
import com.example.netpulse.data.lan.LanScannerRepository
import com.example.netpulse.data.lan.NetworkInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LanScannerUiState(
    val devices: List<LanDevice> = emptyList(),
    val isScanning: Boolean = false,
    val networkInfo: NetworkInfo = NetworkInfo(),
    val searchQuery: String = ""
)

class LanScannerViewModel(
    private val repository: LanScannerRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<LanScannerUiState> = combine(
        repository.discoveredDevices,
        repository.isScanning,
        _searchQuery
    ) { devices, isScanning, query ->
        val distinctDevices = devices.distinctBy { it.ipAddress }
        val filtered = if (query.isBlank()) distinctDevices else {
            distinctDevices.filter {
                it.ipAddress.contains(query, ignoreCase = true) || 
                it.hostname.contains(query, ignoreCase = true) ||
                (it.nickname?.contains(query, ignoreCase = true) == true)
            }
        }
        LanScannerUiState(
            devices = filtered,
            isScanning = isScanning,
            networkInfo = repository.getNetworkInfo(),
            searchQuery = query
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LanScannerUiState())

    fun startScan() {
        repository.startScan()
    }

    fun stopScan() {
        repository.stopScan()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(device: LanDevice) {
        viewModelScope.launch {
            repository.toggleFavorite(device.ipAddress, !device.isFavorite)
        }
    }

    fun renameDevice(device: LanDevice, newName: String?) {
        viewModelScope.launch {
            repository.updateNickname(device.ipAddress, newName)
        }
    }
}

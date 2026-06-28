package com.example.netpulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netpulse.data.datastore.UserPreferences
import com.example.netpulse.data.lan.*
import com.example.netpulse.utils.lan.LanExportUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class LanScannerViewModel(
    private val repository: LanScannerRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _sortOption = MutableStateFlow(LanSortOption.IP)
    private val _filterOption = MutableStateFlow(LanFilterOption.ALL)
    
    private var autoRefreshJob: Job? = null

    init {
        // Handle Auto-Scan based on user preferences
        viewModelScope.launch {
            userPreferences.lanAutoScanInterval.collect { interval ->
                val seconds = when (interval) {
                    "Every 30 Seconds" -> 30L
                    "Every 1 Minute" -> 60L
                    "Every 5 Minutes" -> 300L
                    "Every 10 Minutes" -> 600L
                    else -> 0L
                }
                startAutoRefresh(seconds)
            }
        }
        
        // Handle Scan on App Launch
        viewModelScope.launch {
            if (userPreferences.lanScanOnLaunch.first()) {
                startScan()
            }
        }
    }

    val uiState: StateFlow<LanScanUiState> = combine(
        repository.discoveredDevices,
        repository.isScanning,
        _searchQuery,
        _sortOption,
        _filterOption
    ) { devices, isScanning, query, sort, filter ->
        
        val filtered = devices
            .filter { device ->
                when (filter) {
                    LanFilterOption.ALL -> true
                    LanFilterOption.ONLINE -> device.isOnline
                    LanFilterOption.OFFLINE -> !device.isOnline
                    LanFilterOption.FAVORITES -> device.isFavorite
                    LanFilterOption.UNKNOWN -> device.deviceType == DeviceType.UNKNOWN
                    LanFilterOption.PHONES -> device.deviceType == DeviceType.PHONE
                    LanFilterOption.LAPTOPS -> device.deviceType == DeviceType.LAPTOP
                    LanFilterOption.DESKTOPS -> device.deviceType == DeviceType.DESKTOP
                    LanFilterOption.TVS -> device.deviceType == DeviceType.TV
                    LanFilterOption.PRINTERS -> device.deviceType == DeviceType.PRINTER
                    LanFilterOption.ROUTERS -> device.deviceType == DeviceType.ROUTER
                    LanFilterOption.IOT -> device.deviceType == DeviceType.IOT
                    LanFilterOption.RECENTLY_JOINED -> device.lastSeen > System.currentTimeMillis() - 60000
                }
            }
            .filter { device ->
                query.isBlank() || 
                device.ipAddress.contains(query, ignoreCase = true) || 
                device.hostname.contains(query, ignoreCase = true) ||
                (device.nickname?.contains(query, ignoreCase = true) == true)
            }
            .let { list ->
                when (sort) {
                    LanSortOption.NAME -> list.sortedBy { it.nickname ?: it.hostname }
                    LanSortOption.IP -> list.sortedBy { it.ipAddress.split(".").map { part -> part.padStart(3, '0') }.joinToString(".") }
                    LanSortOption.LATENCY -> list.sortedBy { if (it.latencyMs < 0) Long.MAX_VALUE else it.latencyMs }
                    LanSortOption.LAST_SEEN -> list.sortedByDescending { it.lastSeen }
                    LanSortOption.STATUS -> list.sortedByDescending { it.isOnline }
                    LanSortOption.FAVORITES -> list.sortedByDescending { it.isFavorite }
                    LanSortOption.TYPE -> list.sortedBy { it.deviceType.name }
                }
            }

        val avgLat = if (filtered.any { it.latencyMs > 0 }) filtered.filter { it.latencyMs > 0 }.map { it.latencyMs }.average().toLong() else 0L
        val health = repository.calculateNetworkHealth(devices, avgLat)
        val healthLabel = when {
            health >= 85 -> NetworkQuality.EXCELLENT
            health >= 70 -> NetworkQuality.GOOD
            health >= 40 -> NetworkQuality.FAIR
            else -> NetworkQuality.POOR
        }

        LanScanUiState(
            devices = filtered,
            isScanning = isScanning,
            networkInfo = repository.getNetworkInfo(),
            searchQuery = query,
            sortOption = sort,
            filterOption = filter,
            lastScanTime = System.currentTimeMillis(),
            networkHealthScore = health,
            healthLabel = healthLabel,
            avgLatency = avgLat,
            insights = repository.generateInsights(devices, avgLat)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LanScanUiState())

    fun startScan() {
        repository.startScan()
    }

    fun startAutoRefresh(intervalSeconds: Long) {
        autoRefreshJob?.cancel()
        if (intervalSeconds <= 0) return
        
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                delay(intervalSeconds * 1000)
                if (!uiState.value.isScanning) {
                    repository.startScan()
                }
            }
        }
    }

    fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }

    fun stopScan() {
        repository.stopScan()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
        repository.stopScan()
    }

    fun setSortOption(option: LanSortOption) {
        _sortOption.value = option
    }

    fun setFilterOption(option: LanFilterOption) {
        _filterOption.value = option
    }

    fun toggleFavorite(device: LanDevice) {
        viewModelScope.launch {
            repository.toggleFavorite(device.ipAddress, !device.isFavorite)
        }
    }

    fun updateNickname(device: LanDevice, nickname: String?) {
        viewModelScope.launch {
            repository.updateNickname(device.ipAddress, nickname)
        }
    }

    fun updateNotes(device: LanDevice, notes: String?) {
        viewModelScope.launch {
            repository.updateNotes(device.ipAddress, notes)
        }
    }

    fun exportResults(context: android.content.Context, format: String): File? {
        val devices = uiState.value.devices
        return when (format.uppercase()) {
            "PDF" -> LanExportUtils.exportToPdf(context, devices)
            "CSV" -> LanExportUtils.exportToCsv(context, devices)
            "JSON" -> LanExportUtils.exportToJson(context, devices)
            else -> null
        }
    }
}

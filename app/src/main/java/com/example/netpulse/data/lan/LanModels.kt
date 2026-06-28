package com.example.netpulse.data.lan

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.netpulse.ui.viewmodel.NetworkQuality

@Immutable
data class LanDevice(
    val ipAddress: String,
    val hostname: String = "Unknown",
    val macAddress: String = "Hidden by Android",
    val vendor: String = "Unknown",
    val deviceType: DeviceType = DeviceType.UNKNOWN,
    val latencyMs: Long = -1,
    val isOnline: Boolean = true,
    val isRouter: Boolean = false,
    val isCurrentDevice: Boolean = false,
    val isFavorite: Boolean = false,
    val isUnknown: Boolean = false,
    val nickname: String? = null,
    val notes: String? = null,
    val firstSeen: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis(),
    val connectionDuration: Long = 0,
    val os: String = "Unknown",
    val connectionType: String = "Wi-Fi" // All LAN scanner devices are on network, but could distinguish if it's wired/wireless if we could (usually hard from frontend)
)

enum class DeviceType(val label: String) {
    ROUTER("Router"), 
    PHONE("Phone"), 
    TABLET("Tablet"), 
    LAPTOP("Laptop"), 
    DESKTOP("Desktop"), 
    PRINTER("Printer"), 
    TV("Smart TV"), 
    NAS("NAS"), 
    CAMERA("Camera"), 
    CONSOLE("Game Console"), 
    IOT("IoT Device"), 
    UNKNOWN("Unknown")
}

@Entity(tableName = "lan_device_history")
data class LanDeviceEntity(
    @PrimaryKey val ipAddress: String,
    val hostname: String,
    val macAddress: String,
    val vendor: String,
    val deviceType: String,
    val nickname: String?,
    val notes: String?,
    val isFavorite: Boolean,
    val firstSeen: Long,
    val lastSeen: Long,
    val os: String = "Unknown"
)

data class NetworkInfo(
    val gatewayIp: String = "0.0.0.0",
    val localIp: String = "0.0.0.0",
    val subnetMask: String = "255.255.255.0",
    val ssid: String = "Unknown",
    val dns1: String = "—",
    val dns2: String = "—"
)

data class LanScanUiState(
    val isScanning: Boolean = false,
    val devices: List<LanDevice> = emptyList(),
    val networkInfo: NetworkInfo = NetworkInfo(),
    val searchQuery: String = "",
    val sortOption: LanSortOption = LanSortOption.IP,
    val filterOption: LanFilterOption = LanFilterOption.ALL,
    val lastScanTime: Long = 0,
    val networkHealthScore: Int = 0,
    val healthLabel: NetworkQuality = NetworkQuality.FAIR,
    val avgLatency: Long = 0,
    val insights: List<String> = emptyList()
)

enum class LanSortOption {
    NAME, IP, LATENCY, LAST_SEEN, STATUS, FAVORITES, TYPE
}

enum class LanFilterOption {
    ALL, ONLINE, OFFLINE, FAVORITES, UNKNOWN, PHONES, LAPTOPS, DESKTOPS, TVS, PRINTERS, ROUTERS, IOT, RECENTLY_JOINED
}

package com.example.netpulse.data.lan

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val nickname: String? = null,
    val notes: String? = null,
    val firstSeen: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis()
)

enum class DeviceType {
    ROUTER, PHONE, TABLET, LAPTOP, DESKTOP, PRINTER, TV, NAS, CAMERA, CONSOLE, IOT, UNKNOWN
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
    val lastSeen: Long
)

data class NetworkInfo(
    val gatewayIp: String = "0.0.0.0",
    val localIp: String = "0.0.0.0",
    val subnetMask: String = "255.255.255.0",
    val ssid: String = "Unknown"
)

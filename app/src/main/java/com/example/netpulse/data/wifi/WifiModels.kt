package com.example.netpulse.data.wifi

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val signalStrength: Int,    // dBm e.g. -65
    val frequency: Int,          // MHz e.g. 2437
    val capabilities: String,    // e.g. "[WPA2-PSK][ESS]"
    val channelWidth: String,    // e.g. "80 MHz"
    val standard: String,        // e.g. "802.11ac (WiFi 5)"
    val channel: Int,            // e.g. 6 or 36
    val timestamp: Long,
    val isFavorite: Boolean = false,
    val isConnected: Boolean = false
) {
    val frequencyBand: String get() = when {
        frequency < 3000 -> "2.4 GHz"
        frequency < 6000 -> "5.0 GHz"
        else -> "6.0 GHz"
    }

    val securityType: String get() = when {
        capabilities.contains("WPA3") -> "WPA3"
        capabilities.contains("WPA2") -> "WPA2"
        capabilities.contains("WPA") -> "WPA"
        capabilities.contains("WEP") -> "WEP"
        else -> "Open"
    }

    val isHidden: Boolean get() = ssid.isEmpty()

    val signalPercentage: Float get() {
        val clamped = signalStrength.coerceIn(-100, 0)
        return (clamped + 100) / 100f
    }

    val signalQuality: String get() = when {
        signalStrength > -50 -> "Excellent"
        signalStrength > -60 -> "Good"
        signalStrength > -70 -> "Fair"
        else -> "Weak"
    }
}

@Entity(tableName = "wifi_history")
data class WifiHistoryEntity(
    @PrimaryKey val bssid: String,
    val ssid: String,
    val lastSeen: Long,
    val firstSeen: Long,
    val maxRssi: Int,
    val isFavorite: Boolean = false,
    val connectionCount: Int = 0
)

enum class WifiSortOption {
    SIGNAL, SSID, FREQUENCY, CHANNEL, DISTANCE, SECURITY
}

enum class WifiFilterOption {
    ALL, CONNECTED, BAND_2_4, BAND_5, BAND_6, STRONG, WEAK, OPEN, WPA2, WPA3, FAVORITES, HIDDEN
}

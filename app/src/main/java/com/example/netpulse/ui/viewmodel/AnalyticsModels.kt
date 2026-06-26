package com.example.netpulse.ui.viewmodel

import androidx.compose.ui.graphics.vector.ImageVector

data class NetworkStatus(
    val isConnected: Boolean = false,
    val type: String = "—",
    val ssid: String = "—",
    val bssid: String = "—",
    val security: String = "—",
    val signalStrength: Int = 0,
    val signalLevel: Int = 0,
    val rssi: Int = 0,
    val frequency: String = "—",
    val band: String = "—",
    val channel: Int = 0,
    val linkSpeed: Int = 0,
    val txSpeed: Int = 0,
    val rxSpeed: Int = 0
)

data class InternetDetails(
    val publicIp: String = "—",
    val localIp: String = "—",
    val ipv6: String = "—",
    val gateway: String = "—",
    val dns1: String = "—",
    val dns2: String = "—",
    val mac: String = "—",
    val subnet: String = "—",
    val interfaceName: String = "—"
)

data class IspInfo(
    val name: String = "—",
    val asn: String = "—",
    val org: String = "—",
    val country: String = "—",
    val region: String = "—",
    val city: String = "—",
    val timezone: String = "—"
)

data class SpeedSummary(
    val download: Float = 0f,
    val upload: Float = 0f,
    val ping: Int = 0,
    val jitter: Int = 0,
    val packetLoss: Float = 0.0f,
    val testTime: String = "—",
    val server: String = "—"
)

enum class NetworkQuality(val label: String) {
    EXCELLENT("Excellent"), GOOD("Good"), FAIR("Fair"), POOR("Poor")
}

data class NetworkDataUsage(
    val today: String = "0 MB",
    val weekly: String = "0 MB",
    val monthly: String = "0 MB",
    val history: List<Float> = emptyList()
)

data class DeviceInfo(
    val androidVersion: String = "—",
    val sdk: Int = 0,
    val manufacturer: String = "—",
    val model: String = "—",
    val resolution: String = "—",
    val cpuAbi: String = "—",
    val ram: String = "—",
    val storage: String = "—"
)

data class TimelineEvent(
    val time: String,
    val title: String,
    val description: String,
    val type: TimelineType
)

enum class TimelineType {
    CONNECTED, DISCONNECTED, IP_CHANGED, NETWORK_SWITCHED, TEST_STARTED, TEST_FINISHED
}

data class AdvancedDiagnostics(
    val mtu: Int = 1500,
    val tcpLatency: Int = 0,
    val dnsLookup: Int = 0,
    val handshake: Int = 0,
    val hops: Int = 0
)

data class SecurityStatus(
    val vpnActive: Boolean = false,
    val privateDns: Boolean = false,
    val captivePortal: Boolean = false,
    val metered: Boolean = false,
    val roaming: Boolean = false,
    val validated: Boolean = false
)

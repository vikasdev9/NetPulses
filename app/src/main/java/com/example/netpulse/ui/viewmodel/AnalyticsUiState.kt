package com.example.netpulse.ui.viewmodel

import androidx.compose.ui.graphics.vector.ImageVector

data class AnalyticsUiState(
    val networkStatus: NetworkStatus = NetworkStatus(),
    val internetDetails: InternetDetails = InternetDetails(),
    val ispInfo: IspInfo = IspInfo(),
    val speedSummary: SpeedSummary = SpeedSummary(),
    val networkQuality: NetworkQuality = NetworkQuality.EXCELLENT,
    val dataUsage: DataUsage = DataUsage(),
    val deviceInfo: DeviceInfo = DeviceInfo(),
    val timeline: List<TimelineEvent> = emptyList(),
    val diagnostics: AdvancedDiagnostics = AdvancedDiagnostics(),
    val security: SecurityStatus = SecurityStatus(),
    val recommendations: List<String> = emptyList(),
    val isLoading: Boolean = false
)

data class NetworkStatus(
    val isConnected: Boolean = true,
    val type: String = "WiFi",
    val ssid: String = "NetPulse_5G_Home",
    val security: String = "WPA3-SAE",
    val signalStrength: Int = 92,
    val rssi: Int = -42,
    val frequency: String = "5 GHz",
    val linkSpeed: Int = 866,
    val txSpeed: Int = 780,
    val rxSpeed: Int = 820
)

data class InternetDetails(
    val publicIp: String = "103.24.122.85",
    val localIp: String = "192.168.1.15",
    val ipv6: String = "2405:201:200b:40b6:...",
    val gateway: String = "192.168.1.1",
    val dns: String = "8.8.8.8, 1.1.1.1",
    val mac: String = "00:0a:95:9d:68:16",
    val subnet: String = "255.255.255.0",
    val interfaceName: String = "wlan0"
)

data class IspInfo(
    val name: String = "Reliance Jio Infocomm",
    val asn: String = "AS55836",
    val org: String = "Jio Networks",
    val country: String = "India",
    val region: String = "Maharashtra",
    val city: String = "Mumbai",
    val timezone: String = "Asia/Kolkata"
)

data class SpeedSummary(
    val download: Float = 452.5f,
    val upload: Float = 180.2f,
    val ping: Int = 12,
    val jitter: Int = 3,
    val packetLoss: Float = 0.0f,
    val testTime: String = "2 mins ago",
    val server: String = "Mumbai - Tata Comm"
)

enum class NetworkQuality(val label: String) {
    EXCELLENT("Excellent"), GOOD("Good"), FAIR("Fair"), POOR("Poor")
}

data class DataUsage(
    val today: String = "1.2 GB",
    val weekly: String = "8.5 GB",
    val monthly: String = "42.1 GB",
    val history: List<Float> = listOf(0.4f, 0.8f, 1.2f, 0.9f, 1.5f, 1.1f, 1.2f) // Last 7 days
)

data class DeviceInfo(
    val androidVersion: String = "Android 14",
    val sdk: Int = 34,
    val manufacturer: String = "Google",
    val model: String = "Pixel 8 Pro",
    val resolution: String = "1344 x 2992",
    val cpuAbi: String = "arm64-v8a",
    val ram: String = "12 GB",
    val storage: String = "256 GB"
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
    val tcpLatency: Int = 24,
    val dnsLookup: Int = 18,
    val handshake: Int = 45,
    val hops: Int = 8
)

data class SecurityStatus(
    val vpnActive: Boolean = false,
    val privateDns: Boolean = true,
    val captivePortal: Boolean = false,
    val metered: Boolean = false,
    val roaming: Boolean = false,
    val validated: Boolean = true
)

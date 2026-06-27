package com.example.netpulse.ui.viewmodel

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.netpulse.data.analytics.DailyUsage

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
    val wifiStandard: String = "—",
    val signalPercentage: Int = 0,
    val isHidden: Boolean = false,
    val isRandomizedMac: Boolean = false,
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

data class MobileNetworkInfo(
    val simOperator: String = "—",
    val networkOperator: String = "—",
    val carrierName: String = "—",
    val simCountry: String = "—",
    val mcc: String = "—",
    val mnc: String = "—",
    val roamingStatus: String = "—",
    val networkGeneration: String = "—",
    val signalStrength: String = "—",
    val lteSignalStrength: String = "—",
    val nrSignalStrength: String = "—",
    val cellId: String = "—",
    val tac: String = "—",
    val pci: String = "—",
    val registeredNetwork: String = "—",
    val preferredNetworkType: String = "—"
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
    val server: String = "—",
    val avgDownload: Float = 0f,
    val avgUpload: Float = 0f,
    val peakDownload: Float = 0f,
    val peakUpload: Float = 0f,
    val minPing: Int = 0,
    val maxPing: Int = 0,
    val testDuration: String = "0s"
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
    val deviceName: String = "—",
    val brand: String = "—",
    val manufacturer: String = "—",
    val model: String = "—",
    val product: String = "—",
    val device: String = "—",
    val androidVersion: String = "—",
    val sdk: Int = 0,
    val securityPatch: String = "—",
    val bootloader: String = "—",
    val kernelVersion: String = "—",
    val buildNumber: String = "—",
    val supportedAbis: String = "—",
    val cpuArch: String = "—",
    val cpuCores: Int = 0,
    val board: String = "—",
    val hardware: String = "—",
    
    // Memory
    val totalRam: String = "—",
    val availableRam: String = "—",
    val usedRam: String = "—",
    val isLowMemory: Boolean = false,
    
    // Storage
    val totalStorage: String = "—",
    val availableStorage: String = "—",
    val usedStorage: String = "—",
    val storageUsagePercent: Int = 0,
    
    // Display
    val resolution: String = "—",
    val density: Int = 0,
    val refreshRate: String = "—",
    val screenSize: String = "—",
    val orientation: String = "—",

    // Battery
    val batteryLevel: Int = 0,
    val isCharging: Boolean = false,
    val chargingType: String = "—",
    val batteryHealth: String = "—",
    val batteryTemp: String = "—",
    val batteryVoltage: String = "—",
    val batteryTech: String = "—",
    val batteryCapacity: String = "—",
    val isPowerSaveMode: Boolean = false
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
    val hops: Int = 0,
    val interfaceName: String = "—",
    val ipv6Support: Boolean = false,
    val dualStack: Boolean = false,
    val estimatedRtt: Int = 0,
    val estimatedBandwidth: String = "—",
    val capabilities: String = "—",
    val transportType: String = "—",
    val validationStatus: String = "—"
)

data class SecurityStatus(
    val vpnActive: Boolean = false,
    val privateDns: Boolean = false,
    val privateDnsServer: String = "—",
    val dnsOverTls: Boolean = false,
    val captivePortal: Boolean = false,
    val metered: Boolean = false,
    val roaming: Boolean = false,
    val validated: Boolean = true
)

data class UseCaseRating(
    val gaming: NetworkQuality = NetworkQuality.FAIR,
    val streaming: NetworkQuality = NetworkQuality.FAIR,
    val videoCalls: NetworkQuality = NetworkQuality.FAIR,
    val browsing: NetworkQuality = NetworkQuality.FAIR,
    val downloads: NetworkQuality = NetworkQuality.FAIR
)

data class TrendPoint(val label: String, val value: Float)

enum class TrendPeriod { WEEKLY, MONTHLY }

data class TrendStats(
    val highest: Float = 0f,
    val lowest: Float = 0f,
    val average: Float = 0f,
    val highestLabel: String = "—",
    val lowestLabel: String = "—"
)



data class NetworkUsageStats(
    val today: Long = 0,
    val weekly: Long = 0,
    val monthly: Long = 0,
    val totalMobile: Long = 0,
    val totalWifi: Long = 0,
    val totalFormatted: String = "0 MB"
)

data class StabilityMetrics(
    val uptimePercentage: Int = 0,
    val signalLabel: String = "Good",
    val disconnectionCount: Int = 0,
    val pingStability: Float = 0f,
    val liveSignalStrength: Int = 0
)

data class RecommendationItem(
    val id: String,
    val title: String,
    val description: String,
    val priority: RecommendationPriority,
    val isDismissed: Boolean = false
)

enum class RecommendationPriority { HIGH, MEDIUM, LOW }

data class EstimatedUsage(
    val todayMB: Float = 0f,
    val weekMB: Float = 0f,
    val monthMB: Float = 0f,
    val planLimitGB: Float = 100f
)

data class IspPerformance(
    val deliveryScore: Int = 0,
    val reliabilityScore: Int = 0,
    val rankBadge: String = "Average",
    val actualAvg: Float = 0f,
    val advertised: Float = 100f
)

data class NetworkSummary(
    val title: String, // Today, Weekly, Monthly
    val testCount: Int = 0,
    val avgDownload: Float = 0f,
    val avgUpload: Float = 0f,
    val avgPing: Int = 0,
    val bestDay: String = "—",
    val range: AnalyticsRange
)

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val selectedRange: AnalyticsRange = AnalyticsRange.TODAY,
    val dashboardTab: DashboardTab = DashboardTab.DATA,
    val networkStatus: NetworkStatus = NetworkStatus(),
    val mobileNetworkInfo: MobileNetworkInfo = MobileNetworkInfo(),
    val internetDetails: InternetDetails = InternetDetails(),
    val ispInfo: IspInfo = IspInfo(),
    val speedSummary: SpeedSummary = SpeedSummary(),
    val networkQuality: NetworkQuality = NetworkQuality.FAIR,
    val deviceInfo: DeviceInfo = DeviceInfo(),
    val timeline: List<TimelineEvent> = emptyList(),
    val diagnostics: AdvancedDiagnostics = AdvancedDiagnostics(),
    val security: SecurityStatus = SecurityStatus(),
    val recommendations: List<RecommendationItem> = emptyList(),
    val healthScore: Int = 0,
    val healthTrend: Float = 0f,
    val useCaseRating: UseCaseRating = UseCaseRating(),
    val wifiData: NetworkUsageStats = NetworkUsageStats(),
    val mobileData: NetworkUsageStats = NetworkUsageStats(),
    val weeklyUsage: List<DailyUsage> = emptyList(),
    val perAppData: List<com.example.netpulse.data.analytics.AppDataUsage> = emptyList(),
    val totalScreenTimeMs: Long = 0L,
    val perAppScreenTime: List<com.example.netpulse.data.analytics.AppScreenTime> = emptyList(),
    val weeklyScreenTime: List<com.example.netpulse.data.analytics.DailyScreenTime> = emptyList(),
    val top3Apps: List<com.example.netpulse.data.analytics.CombinedAppUsage> = emptyList(),
    val allAppsCombined: List<com.example.netpulse.data.analytics.CombinedAppUsage> = emptyList(),
    val hasUsagePermission: Boolean = false,
    val tipApp: com.example.netpulse.data.analytics.AppDataUsage? = null,
    val streak: Int = 0,
    val recentStreakDays: List<Boolean> = emptyList(),
    val achievements: List<com.example.netpulse.data.analytics.Achievement> = emptyList(),
    val stabilityMetrics: StabilityMetrics = StabilityMetrics(),
    val estimatedUsage: EstimatedUsage = EstimatedUsage(),
    val ispPerformance: IspPerformance = IspPerformance(),
    val trendData: List<TrendPoint> = emptyList(),
    val trendPeriod: TrendPeriod = TrendPeriod.WEEKLY,
    val trendStats: TrendStats = TrendStats(),
    val summaries: List<NetworkSummary> = emptyList()
)

enum class AnalyticsRange { TODAY, WEEK, MONTH }
enum class DashboardTab { DATA, TIME }

package com.example.netpulse.ui.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.netpulse.data.analytics.*
import com.example.netpulse.data.repository.NetworkRepository
import com.example.netpulse.insights.achievements.AchievementRepository
import com.example.netpulse.insights.usage.UsageRepository
import com.example.netpulse.insights.isp.ISPRepository
import com.example.netpulse.insights.wifistability.WifiStabilityRepository
import com.example.netpulse.data.datastore.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

enum class AnalyticsRange { TODAY, WEEK, MONTH }
enum class AnalyticsTab { DATA, TIME }
enum class DashboardTab { DATA, TIME }

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val selectedRange: AnalyticsRange = AnalyticsRange.TODAY,
    val selectedTab: AnalyticsTab = AnalyticsTab.DATA,
    val dashboardTab: DashboardTab = DashboardTab.DATA,
    
    // Usage Insights (System APIs)
    val mobileData: DataUsage = DataUsage(),
    val wifiData: DataUsage = DataUsage(),
    val weeklyUsage: List<DailyUsage> = emptyList(),
    val perAppData: List<AppDataUsage> = emptyList(),
    val totalScreenTimeMs: Long = 0L,
    val perAppScreenTime: List<AppScreenTime> = emptyList(),
    val weeklyScreenTime: List<DailyScreenTime> = emptyList(),
    val top3Apps: List<CombinedAppUsage> = emptyList(),
    val allAppsCombined: List<CombinedAppUsage> = emptyList(),
    val hasUsagePermission: Boolean = false,
    val tipApp: AppDataUsage? = null,

    // Network Intelligence (Repository)
    val networkStatus: NetworkStatus = NetworkStatus(),
    val internetDetails: InternetDetails = InternetDetails(),
    val ispInfo: IspInfo = IspInfo(),
    val speedSummary: SpeedSummary = SpeedSummary(),
    val networkQuality: NetworkQuality = NetworkQuality.FAIR,
    val deviceInfo: DeviceInfo = DeviceInfo(),
    val timeline: List<TimelineEvent> = emptyList(),
    val diagnostics: AdvancedDiagnostics = AdvancedDiagnostics(),
    val security: SecurityStatus = SecurityStatus(),
    val recommendations: List<RecommendationItem> = emptyList(),

    // FEATURE 1: Health Score
    val healthScore: Int = 0,
    val healthTrend: Float = 0f, // diff from yesterday

    // FEATURE 3: Trends
    val trendData: List<TrendPoint> = emptyList(),
    val trendPeriod: TrendPeriod = TrendPeriod.WEEKLY,
    val trendStats: TrendStats = TrendStats(),

    // FEATURE 4: Usage (Estimated from tests)
    val estimatedUsage: EstimatedUsage = EstimatedUsage(),

    // FEATURE 5: ISP Performance
    val ispPerformance: IspPerformance = IspPerformance(),

    // FEATURE 7: Achievements
    val achievements: List<Achievement> = emptyList(),
    val streak: Int = 0,
    val recentStreakDays: List<Boolean> = emptyList(),

    // FEATURE 8: Wi-Fi Stability
    val stabilityMetrics: StabilityMetrics = StabilityMetrics()
)

data class RecommendationItem(
    val id: String,
    val title: String,
    val description: String,
    val priority: RecommendationPriority,
    val isDismissed: Boolean = false
)

enum class RecommendationPriority { HIGH, MEDIUM, LOW }

data class TrendPoint(val label: String, val value: Float)
enum class TrendPeriod { WEEKLY, MONTHLY }
data class TrendStats(val highest: Float = 0f, val lowest: Float = 0f, val average: Float = 0f, val highestLabel: String = "", val lowestLabel: String = "")

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

data class StabilityMetrics(
    val uptimePercentage: Int = 0,
    val signalLabel: String = "Good",
    val disconnectionCount: Int = 0,
    val pingStability: Float = 0f,
    val liveSignalStrength: Int = 0
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataUsageHelper = DataUsageHelper(application)
    private val screenTimeHelper = ScreenTimeHelper(application)
    private val networkRepository = NetworkRepository(application)
    private val achievementRepository = AchievementRepository(application)
    private val usageRepository = UsageRepository(application)
    private val ispRepository = ISPRepository(application)
    private val stabilityRepository = WifiStabilityRepository(application)
    private val userPreferences = UserPreferences(application)
    private val packageManager = application.packageManager
    private val dao = (application as com.example.netpulse.NetPulseApplication).database.speedResultDao()

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // 1. Fetch Network Intelligence (Reactive)
            val networkIntelligenceFlow = combine(
                networkRepository.getNetworkStatus(),
                networkRepository.getInternetDetails(),
                networkRepository.getIspInfo(),
                networkRepository.getSpeedSummary(),
                networkRepository.getDeviceInfo(),
                networkRepository.getTimeline(),
                networkRepository.getDiagnostics(),
                networkRepository.getSecurityStatus(),
                networkRepository.getRecommendations()
            ) { results ->
                results
            }

            // 2. Fetch Usage Insights (One-shot for the current range)
            withContext(Dispatchers.IO) {
                val hasPermission = screenTimeHelper.hasUsagePermission()
                val todayMobile = dataUsageHelper.getTodayMobileData()
                val todayWifi = dataUsageHelper.getTodayWifiData()
                val weeklyUsage = dataUsageHelper.getWeeklyDailyUsage()
                
                val calendar = Calendar.getInstance()
                val endTime = calendar.timeInMillis
                
                when (_uiState.value.selectedRange) {
                    AnalyticsRange.TODAY -> calendar.set(Calendar.HOUR_OF_DAY, 0)
                    AnalyticsRange.WEEK -> calendar.add(Calendar.DAY_OF_YEAR, -7)
                    AnalyticsRange.MONTH -> calendar.add(Calendar.MONTH, -1)
                }
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startTime = calendar.timeInMillis

                val perAppData = dataUsageHelper.getPerAppDataUsage(startTime, endTime)
                val perAppDataWithIcons = perAppData.map { 
                    it.copy(appIcon = try { packageManager.getApplicationIcon(it.packageName) } catch (e: Exception) { null })
                }

                var totalTime = 0L
                var perAppTime = emptyList<AppScreenTime>()
                var weeklyTime = emptyList<DailyScreenTime>()
                
                if (hasPermission) {
                    totalTime = screenTimeHelper.getTodayTotalScreenTime()
                    perAppTime = screenTimeHelper.getPerAppScreenTime(startTime, endTime)
                        .map { it.copy(appIcon = try { packageManager.getApplicationIcon(it.packageName) } catch (e: Exception) { null }) }
                    weeklyTime = screenTimeHelper.getWeeklyScreenTime()
                }

                val tipApp = perAppData.firstOrNull { it.totalBytes > 500 * 1024 * 1024 }
                
                val combinedList = combineAppData(perAppDataWithIcons, perAppTime)
                val sortedByDefault = combinedList.sortedByDescending { it.totalBytes }

                // FEATURE DATA
                val allTests = dao.getAll().first()
                val streakCount = achievementRepository.calculateStreak(allTests)
                
                val usageToday = usageRepository.todayMB.first()
                val usageWeek = usageRepository.weekMB.first()
                val usageMonth = usageRepository.monthMB.first()
                val actualLimit = usageRepository.planLimitGB.first()

                val ispPerf = ispRepository.getPerformance(uiState.value.ispInfo.name, userPreferences.advertisedSpeed.first())
                val stability = stabilityRepository.getMetrics()
                
                // Trend Data Calculation
                val trendPoints = calculateTrendPoints(allTests, _uiState.value.trendPeriod)

                // Collect the network intelligence results once
                val networkResults = networkIntelligenceFlow.first()

                withContext(Dispatchers.Main) {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            hasUsagePermission = hasPermission,
                            mobileData = todayMobile,
                            wifiData = todayWifi,
                            weeklyUsage = weeklyUsage,
                            perAppData = perAppDataWithIcons.take(8),
                            totalScreenTimeMs = totalTime,
                            perAppScreenTime = perAppTime.take(8),
                            weeklyScreenTime = weeklyTime,
                            top3Apps = sortedByDefault.take(3),
                            allAppsCombined = sortCombinedList(combinedList, state.dashboardTab),
                            tipApp = tipApp,
                            
                            // Map Network Intelligence
                            networkStatus = networkResults[0] as NetworkStatus,
                            internetDetails = networkResults[1] as InternetDetails,
                            ispInfo = networkResults[2] as IspInfo,
                            speedSummary = networkResults[3] as SpeedSummary,
                            deviceInfo = networkResults[4] as DeviceInfo,
                            timeline = networkResults[5] as List<TimelineEvent>,
                            diagnostics = networkResults[6] as AdvancedDiagnostics,
                            security = networkResults[7] as SecurityStatus,
                            recommendations = networkResults[8] as List<RecommendationItem>,

                            // Feature Data
                            healthScore = networkRepository.calculateHealthScore(
                                (networkResults[3] as SpeedSummary).download,
                                (networkResults[3] as SpeedSummary).upload,
                                (networkResults[3] as SpeedSummary).ping,
                                (networkResults[3] as SpeedSummary).jitter.toFloat()
                            ),
                            streak = streakCount,
                            estimatedUsage = EstimatedUsage(usageToday, usageWeek, usageMonth, actualLimit),
                            ispPerformance = ispPerf,
                            stabilityMetrics = stability,
                            trendData = trendPoints,
                            trendStats = calculateTrendStats(trendPoints)
                        )
                    }
                }
            }
        }
    }

    private fun calculateTrendPoints(results: List<com.example.netpulse.data.SpeedResult>, period: TrendPeriod): List<TrendPoint> {
        if (results.isEmpty()) return emptyList()
        val daysToFetch = if (period == TrendPeriod.WEEKLY) 7 else 30
        val calendar = Calendar.getInstance()
        val points = mutableListOf<TrendPoint>()

        for (i in 0 until daysToFetch) {
            val calCopy = calendar.clone() as Calendar
            calCopy.add(Calendar.DAY_OF_YEAR, -i)
            calCopy.set(Calendar.HOUR_OF_DAY, 0)
            calCopy.set(Calendar.MINUTE, 0)
            val dayStart = calCopy.timeInMillis
            
            calCopy.set(Calendar.HOUR_OF_DAY, 23)
            calCopy.set(Calendar.MINUTE, 59)
            val dayEnd = calCopy.timeInMillis
            
            val dayResults = results.filter { it.timestamp in dayStart..dayEnd }
            val avg = if (dayResults.isNotEmpty()) dayResults.map { it.downloadMbps }.average().toFloat() else 0f
            
            val label = java.text.SimpleDateFormat("EEE", Locale.getDefault()).format(Date(dayStart))
            points.add(0, TrendPoint(label, avg))
        }
        return points
    }

    private fun calculateTrendStats(points: List<TrendPoint>): TrendStats {
        if (points.isEmpty()) return TrendStats()
        val valid = points.filter { it.value > 0 }
        if (valid.isEmpty()) return TrendStats()
        
        val maxPoint = valid.maxByOrNull { it.value }!!
        val minPoint = valid.minByOrNull { it.value }!!
        val avg = valid.map { it.value }.average().toFloat()
        
        return TrendStats(maxPoint.value, minPoint.value, avg, maxPoint.label, minPoint.label)
    }

    private fun combineAppData(
        dataUsageList: List<AppDataUsage>,
        screenTimeList: List<AppScreenTime>
    ): List<CombinedAppUsage> {
        val combinedMap = mutableMapOf<String, CombinedAppUsage>()

        dataUsageList.forEach { data ->
            combinedMap[data.packageName] = CombinedAppUsage(
                packageName = data.packageName,
                appName = data.appName,
                appIcon = data.appIcon,
                rxBytes = data.rxBytes,
                txBytes = data.txBytes,
                totalBytes = data.totalBytes,
                totalBytesFormatted = data.totalFormatted,
                screenTimeMs = 0L,
                screenTimeFormatted = "< 1m",
                lastUsedLabel = "Never"
            )
        }

        screenTimeList.forEach { time ->
            val existing = combinedMap[time.packageName]
            if (existing != null) {
                combinedMap[time.packageName] = existing.copy(
                    screenTimeMs = time.totalTimeMs,
                    screenTimeFormatted = time.formattedTime,
                    lastUsedLabel = time.lastUsedLabel
                )
            } else {
                combinedMap[time.packageName] = CombinedAppUsage(
                    packageName = time.packageName,
                    appName = time.appName,
                    appIcon = time.appIcon,
                    rxBytes = 0L,
                    txBytes = 0L,
                    totalBytes = 0L,
                    totalBytesFormatted = "0 B",
                    screenTimeMs = time.totalTimeMs,
                    screenTimeFormatted = time.formattedTime,
                    lastUsedLabel = time.lastUsedLabel
                )
            }
        }

        return combinedMap.values.toList()
    }

    private fun sortCombinedList(list: List<CombinedAppUsage>, tab: DashboardTab): List<CombinedAppUsage> {
        return when (tab) {
            DashboardTab.DATA -> list.sortedByDescending { it.totalBytes }
            DashboardTab.TIME -> list.sortedByDescending { it.screenTimeMs }
        }
    }

    fun setDateRange(range: AnalyticsRange) {
        _uiState.update { it.copy(selectedRange = range) }
        loadAnalytics()
    }

    fun setAppListTab(tab: AnalyticsTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setDashboardTab(tab: DashboardTab) {
        _uiState.update { state ->
            state.copy(
                dashboardTab = tab,
                allAppsCombined = sortCombinedList(state.allAppsCombined, tab)
            )
        }
    }

    fun openUsageSettings() {
        screenTimeHelper.openUsageSettings()
    }

    fun refreshData() {
        loadAnalytics()
    }
}

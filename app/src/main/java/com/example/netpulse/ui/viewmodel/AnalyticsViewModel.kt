package com.example.netpulse.ui.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.netpulse.data.analytics.*
import com.example.netpulse.data.repository.NetworkRepository
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
    val recommendations: List<String> = emptyList()
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataUsageHelper = DataUsageHelper(application)
    private val screenTimeHelper = ScreenTimeHelper(application)
    private val networkRepository = NetworkRepository(application)
    private val packageManager = application.packageManager

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

                // Collect the network intelligence results once (or we could keep the flow open)
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
                            recommendations = networkResults[8] as List<String>
                        )
                    }
                }
            }
        }
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

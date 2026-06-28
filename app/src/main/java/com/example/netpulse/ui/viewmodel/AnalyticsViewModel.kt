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
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.*

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
            
            try {
                // 1. Fetch Network Intelligence (Reactive)
                val networkIntelligenceFlow = combine(
                    networkRepository.getNetworkStatus(),
                    networkRepository.getMobileNetworkInfo(),
                    networkRepository.getInternetDetails(),
                    networkRepository.getIspInfo(),
                    networkRepository.getSpeedSummary(),
                    networkRepository.getDeviceInfo(),
                    networkRepository.getTimeline(),
                    networkRepository.getDiagnostics(),
                    networkRepository.getSecurityStatus(),
                    networkRepository.getRecommendations()
                ) { results -> results }
    
                // 2. Fetch Usage Insights
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
                    val perAppDataWithIcons = perAppData.take(15).map { 
                        it.copy(appIcon = try { packageManager.getApplicationIcon(it.packageName) } catch (e: Exception) { null })
                    }
    
                    var totalTime = 0L
                    var perAppTime = emptyList<AppScreenTime>()
                    var weeklyTime = emptyList<DailyScreenTime>()
                    
                    if (hasPermission) {
                        totalTime = screenTimeHelper.getTodayTotalScreenTime()
                        perAppTime = screenTimeHelper.getPerAppScreenTime(startTime, endTime)
                            .take(15)
                            .map { it.copy(appIcon = try { packageManager.getApplicationIcon(it.packageName) } catch (e: Exception) { null }) }
                        weeklyTime = screenTimeHelper.getWeeklyScreenTime()
                    }
    
                    val combinedList = combineAppData(perAppDataWithIcons, perAppTime)
                    val tipApp = perAppData.firstOrNull { it.totalBytes > 500 * 1024 * 1024 }
                    val sortedByDefault = combinedList.sortedByDescending { it.totalBytes }
    
                    val allTests = dao.getAll().first()
                    val streakCount = achievementRepository.calculateStreak(allTests)
                    
                    val usageToday = usageRepository.todayMB.first()
                    val usageWeek = usageRepository.weekMB.first()
                    val usageMonth = usageRepository.monthMB.first()
                    val actualLimit = usageRepository.planLimitGB.first()
    
                    val ispPerf = ispRepository.getPerformance(uiState.value.ispInfo.name, userPreferences.advertisedSpeed.first())
                    val stability = stabilityRepository.getMetrics()
                    
                    val trendPoints = calculateTrendPoints(allTests, _uiState.value.trendPeriod)
    
                    // Collect network intelligence with a timeout to prevent hanging
                    val networkResults = withTimeoutOrNull(5000) { networkIntelligenceFlow.first() }
                    val summaries = calculateSummaries(allTests)
                    
                    if (networkResults != null) {
                        val currentSpeedSummary = networkResults[4] as SpeedSummary
                        val healthScore = networkRepository.calculateHealthScore(
                            currentSpeedSummary.download,
                            currentSpeedSummary.upload,
                            currentSpeedSummary.ping,
                            currentSpeedSummary.jitter.toFloat()
                        )
                        val useCaseRating = calculateUseCaseRating(
                            currentSpeedSummary.download,
                            currentSpeedSummary.ping,
                            currentSpeedSummary.jitter
                        )
    
                        withContext(Dispatchers.Main) {
                            _uiState.update { state ->
                                state.copy(
                                    isLoading = false,
                                    hasUsagePermission = hasPermission,
                                    mobileData = NetworkUsageStats(today = todayMobile.totalBytes, totalFormatted = todayMobile.totalFormatted),
                                    wifiData = NetworkUsageStats(today = todayWifi.totalBytes, totalFormatted = todayWifi.totalFormatted),
                                    weeklyUsage = weeklyUsage,
                                    perAppData = perAppDataWithIcons.take(8),
                                    totalScreenTimeMs = totalTime,
                                    perAppScreenTime = perAppTime.take(8),
                                    weeklyScreenTime = weeklyTime,
                                    top3Apps = sortedByDefault.take(3),
                                    allAppsCombined = sortCombinedList(combinedList, state.dashboardTab),
                                    tipApp = tipApp,
                                    
                                    networkStatus = networkResults[0] as NetworkStatus,
                                    mobileNetworkInfo = networkResults[1] as MobileNetworkInfo,
                                    internetDetails = networkResults[2] as InternetDetails,
                                    ispInfo = networkResults[3] as IspInfo,
                                    speedSummary = currentSpeedSummary,
                                    deviceInfo = networkResults[5] as DeviceInfo,
                                    timeline = networkResults[6] as List<TimelineEvent>,
                                    diagnostics = networkResults[7] as AdvancedDiagnostics,
                                    security = networkResults[8] as SecurityStatus,
                                    recommendations = networkResults[9] as List<RecommendationItem>,
    
                                    healthScore = healthScore,
                                    useCaseRating = useCaseRating,
                                    streak = streakCount,
                                    estimatedUsage = EstimatedUsage(usageToday, usageWeek, usageMonth, actualLimit),
                                    ispPerformance = ispPerf,
                                    stabilityMetrics = stability,
                                    trendData = trendPoints,
                                    trendStats = calculateTrendStats(trendPoints),
                                    summaries = summaries
                                )
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun calculateSummaries(allResults: List<com.example.netpulse.data.SpeedResult>): List<NetworkSummary> {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        // Today
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val todayStart = calendar.timeInMillis
        val todaySummary = createSummaryForRange("Today", allResults.filter { it.timestamp >= todayStart }, AnalyticsRange.TODAY)

        // Weekly
        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val weekStart = calendar.timeInMillis
        val weeklySummary = createSummaryForRange("Weekly", allResults.filter { it.timestamp >= weekStart }, AnalyticsRange.WEEK)

        // Monthly
        calendar.timeInMillis = now
        calendar.add(Calendar.MONTH, -1)
        val monthStart = calendar.timeInMillis
        val monthlySummary = createSummaryForRange("Monthly", allResults.filter { it.timestamp >= monthStart }, AnalyticsRange.MONTH)

        return listOf(todaySummary, weeklySummary, monthlySummary)
    }

    private fun createSummaryForRange(title: String, results: List<com.example.netpulse.data.SpeedResult>, range: AnalyticsRange): NetworkSummary {
        if (results.isEmpty()) return NetworkSummary(title = title, range = range)

        val avgDown = results.map { it.downloadMbps }.average().toFloat()
        val avgUp = results.map { it.uploadMbps }.average().toFloat()
        val avgPing = results.map { it.pingMs }.average().toInt()

        // Group by day to find best day (highest avg download)
        val bestDay = results.groupBy { 
            java.text.SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(it.timestamp))
        }.maxByOrNull { entry -> entry.value.map { it.downloadMbps }.average() }?.key ?: "—"

        return NetworkSummary(
            title = title,
            testCount = results.size,
            avgDownload = avgDown,
            avgUpload = avgUp,
            avgPing = avgPing,
            bestDay = bestDay,
            range = range
        )
    }

    private fun calculateTrendPoints(results: List<com.example.netpulse.data.SpeedResult>, period: TrendPeriod): List<TrendPoint> {
        if (results.isEmpty()) return emptyList()
        val daysToFetch = if (period == TrendPeriod.WEEKLY) 7 else 30
        
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val points = mutableListOf<TrendPoint>()
        val sdf = java.text.SimpleDateFormat("EEE", Locale.getDefault())

        // Optimization: Filter results once for the entire period
        val periodStart = calendar.timeInMillis - ((daysToFetch - 1) * 24 * 60 * 60 * 1000L)
        val relevantResults = results.filter { it.timestamp >= periodStart }
        
        for (i in 0 until daysToFetch) {
            val dayStart = calendar.timeInMillis
            val dayEnd = dayStart + (24 * 60 * 60 * 1000L) - 1
            
            val dayResults = relevantResults.filter { it.timestamp in dayStart..dayEnd }
            val avg = if (dayResults.isNotEmpty()) dayResults.map { it.downloadMbps }.average().toFloat() else 0f
            
            points.add(0, TrendPoint(sdf.format(Date(dayStart)), avg))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return points
    }

    private fun calculateUseCaseRating(download: Float, ping: Int, jitter: Int): UseCaseRating {
        fun rate(score: Int): NetworkQuality = when {
            score >= 90 -> NetworkQuality.EXCELLENT
            score >= 70 -> NetworkQuality.GOOD
            score >= 40 -> NetworkQuality.FAIR
            else -> NetworkQuality.POOR
        }

        val gamingScore = (if (ping < 30) 60 else if (ping < 60) 40 else 10) +
                         (if (jitter < 5) 40 else if (jitter < 15) 20 else 0)
        
        val streamingScore = (if (download > 25) 70 else if (download > 10) 50 else 20) +
                            (if (ping < 100) 30 else 0)
        
        val videoCallScore = (if (download > 5) 50 else 20) +
                            (if (ping < 50) 50 else if (ping < 100) 30 else 0)
        
        val browsingScore = (if (download > 2) 60 else 30) +
                           (if (ping < 150) 40 else 20)
        
        val downloadScore = (if (download > 100) 100 else if (download > 50) 80 else if (download > 20) 60 else 30)

        return UseCaseRating(
            gaming = rate(gamingScore),
            streaming = rate(streamingScore),
            videoCalls = rate(videoCallScore),
            browsing = rate(browsingScore),
            downloads = rate(downloadScore)
        )
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

    fun runDiagnosticsAgain() {
        viewModelScope.launch {
            try {
                loadAnalytics()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun copyIp(context: android.content.Context) {
        val ip = _uiState.value.internetDetails.publicIp
        if (ip != "Fetching..." && ip != "—" && ip.isNotBlank()) {
            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Public IP", ip)
            clipboard.setPrimaryClip(clip)
            android.widget.Toast.makeText(context, "IP copied to clipboard: $ip", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            android.widget.Toast.makeText(context, "Public IP not available yet", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun shareAnalytics(context: android.content.Context) {
        try {
            val state = _uiState.value
            val text = """
                📊 NetPulse Analytics Summary
                ----------------------------
                Health Score: ${state.healthScore}/100
                
                🌐 Connection: ${state.networkStatus.type} (${state.networkStatus.ssid})
                📶 Signal: ${state.networkStatus.signalPercentage}%
                🚀 Latest Speed: ${state.speedSummary.download.toInt()}/${state.speedSummary.upload.toInt()} Mbps
                📡 Latency: ${state.speedSummary.ping} ms | Jitter: ${state.speedSummary.jitter} ms
                
                📍 ISP: ${state.ispInfo.name}
                🌍 Location: ${state.ispInfo.city}, ${state.ispInfo.country}
                
                📱 Device: ${state.deviceInfo.manufacturer} ${state.deviceInfo.model}
                🔋 Battery: ${state.deviceInfo.batteryLevel}% (${state.deviceInfo.batteryHealth})
                
                Generated by NetPulse app
            """.trimIndent()
    
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_SUBJECT, "NetPulse Network Analytics")
                putExtra(android.content.Intent.EXTRA_TEXT, text)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(android.content.Intent.createChooser(intent, "Share Analytics"))
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "Failed to share analytics", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun exportPdf(context: android.content.Context) {
        // Basic PDF generation using PdfDocument
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val pdfDocument = android.graphics.pdf.PdfDocument()
                    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas = page.canvas
                    val paint = android.graphics.Paint()
                    
                    var y = 50f
                    paint.textSize = 18f
                    paint.isFakeBoldText = true
                    canvas.drawText("NetPulse Network Analytics Report", 50f, y, paint)
                    
                    y += 40f
                    paint.textSize = 12f
                    paint.isFakeBoldText = false
                    val state = _uiState.value
                    
                    val lines = listOf(
                        "Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}",
                        "Health Score: ${state.healthScore}",
                        "",
                        "NETWORK STATUS",
                        "Type: ${state.networkStatus.type}",
                        "SSID: ${state.networkStatus.ssid}",
                        "Signal: ${state.networkStatus.signalPercentage}%",
                        "",
                        "SPEED PERFORMANCE",
                        "Download: ${state.speedSummary.download} Mbps",
                        "Upload: ${state.speedSummary.upload} Mbps",
                        "Ping: ${state.speedSummary.ping} ms",
                        "Jitter: ${state.speedSummary.jitter} ms",
                        "",
                        "INTERNET DETAILS",
                        "Public IP: ${state.internetDetails.publicIp}",
                        "ISP: ${state.ispInfo.name}",
                        "ASN: ${state.ispInfo.asn}",
                        "Location: ${state.ispInfo.city}, ${state.ispInfo.country}",
                        "",
                        "DEVICE INFORMATION",
                        "Manufacturer: ${state.deviceInfo.manufacturer}",
                        "Model: ${state.deviceInfo.model}",
                        "Android Version: ${state.deviceInfo.androidVersion}",
                        "RAM: ${state.deviceInfo.totalRam}"
                    )
                    
                    for (line in lines) {
                        canvas.drawText(line, 50f, y, paint)
                        y += 20f
                    }
                    
                    pdfDocument.finishPage(page)
                    
                    val file = File(context.getExternalFilesDir(null), "NetPulse_Report_${System.currentTimeMillis()}.pdf")
                    pdfDocument.writeTo(java.io.FileOutputStream(file))
                    pdfDocument.close()
                    
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(context, "PDF saved to: ${file.absolutePath}", android.widget.Toast.LENGTH_LONG).show()
                        
                        // Try to open it
                        try {
                            val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                             // Fallback if no PDF viewer
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(context, "Failed to export PDF", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

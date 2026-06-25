package com.example.netpulse.data.analytics

import android.graphics.drawable.Drawable
import java.util.Date

enum class TimeOfDay { MORNING, AFTERNOON, EVENING, NIGHT }

data class HealthBadge(val label: String, val status: BadgeStatus)
enum class BadgeStatus { GOOD, FAIR, POOR, EXCELLENT }

data class SmartInsight(
    val title: String,
    val description: String,
    val type: InsightType
)
enum class InsightType { WARNING, SUCCESS, INFO, ALERT }

data class StreakDay(val dayLabel: String, val status: StreakStatus)
enum class StreakStatus { COMPLETED, MISSED, TODAY }

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val unlockedDate: Long? = null
)

enum class SignalStrength { EXCELLENT, GOOD, FAIR, WEAK }

data class Recommendation(
    val id: String,
    val title: String,
    val description: String,
    val priority: RecommendationPriority,
    val isDismissed: Boolean = false
)
enum class RecommendationPriority { HIGH, MEDIUM, LOW }

data class DailyReport(
    val bestSpeed: Float,
    val worstSpeed: Float,
    val averageSpeed: Float,
    val totalTests: Int,
    val bestHour: Int,
    val worstHour: Int,
    val date: Long
)

data class DataUsage(
    val rxBytes: Long = 0L,
    val txBytes: Long = 0L
) {
    val totalBytes: Long get() = rxBytes + txBytes
    val totalFormatted: String get() = formatBytes(totalBytes)
}

data class AppDataUsage(
    val uid: Int,
    val packageName: String,
    val appName: String,
    val appIcon: Drawable?,
    val rxBytes: Long,
    val txBytes: Long
) {
    val totalBytes: Long get() = rxBytes + txBytes
    val totalFormatted: String get() = formatBytes(totalBytes)
}

data class DailyUsage(
    val date: Date,
    val dayLabel: String,
    val mobileRx: Long,
    val mobileTx: Long,
    val wifiRx: Long,
    val wifiTx: Long
) {
    val totalMobile: Long get() = mobileRx + mobileTx
    val totalWifi: Long get() = wifiRx + wifiTx
    val totalAll: Long get() = totalMobile + totalWifi
}

data class AppScreenTime(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable?,
    val totalTimeMs: Long,
    val lastUsedMs: Long,
    val lastUsedLabel: String,
    val formattedTime: String
)

data class DailyScreenTime(
    val date: Date,
    val dayLabel: String,
    val totalMs: Long,
    val formatted: String
)

data class CombinedAppUsage(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable?,
    val rxBytes: Long,
    val txBytes: Long,
    val totalBytes: Long,
    val totalBytesFormatted: String,
    val screenTimeMs: Long,
    val screenTimeFormatted: String,
    val lastUsedLabel: String
)

data class SpeedTestResult(
    val downloadMbps: Float,
    val uploadMbps: Float,
    val pingMs: Int,
    val jitterMs: Float,
    val timestamp: Long,
    val timeOfDay: TimeOfDay
)

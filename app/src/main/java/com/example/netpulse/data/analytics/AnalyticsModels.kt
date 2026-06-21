package com.example.netpulse.data.analytics

import android.graphics.drawable.Drawable
import java.util.Date

data class DataUsage(
    val rxBytes: Long = 0L,
    val txBytes: Long = 0L
) {
    val totalBytes: Long = rxBytes + txBytes
    val totalFormatted: String = formatBytes(totalBytes)
    val rxFormatted: String = formatBytes(rxBytes)
    val txFormatted: String = formatBytes(txBytes)
}

data class AppDataUsage(
    val uid: Int,
    val packageName: String,
    val appName: String,
    val appIcon: Drawable?,
    val rxBytes: Long,
    val txBytes: Long
) {
    val totalBytes: Long = rxBytes + txBytes
    val totalFormatted: String = formatBytes(totalBytes)
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

data class DailyUsage(
    val date: Date,
    val dayLabel: String,
    val mobileRx: Long = 0L,
    val mobileTx: Long = 0L,
    val wifiRx: Long = 0L,
    val wifiTx: Long = 0L
) {
    val totalMobile: Long = mobileRx + mobileTx
    val totalWifi: Long = wifiRx + wifiTx
    val totalAll: Long = totalMobile + totalWifi
}

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

fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
    val pre = "KMGTPE"[exp - 1]
    return String.format("%.2f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
}

fun formatDuration(ms: Long): String {
    if (ms < 60000) return "< 1m"
    val minutes = (ms / 60000) % 60
    val hours = ms / 3600000
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

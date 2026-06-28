package com.example.netpulse.thermal.model

import androidx.compose.ui.graphics.Color

enum class ThermalStatus(val label: String, val color: Color) {
    COOL("Cool", Color(0xFF00E676)),
    NORMAL("Normal", Color(0xFF3B8BFF)),
    WARM("Warm", Color(0xFFFFB300)),
    HOT("Hot", Color(0xFFFF6D00)),
    CRITICAL("Critical", Color(0xFFEF4444)),
    UNKNOWN("Unknown", Color(0xFF8892A4))
}

data class ThermalData(
    val temperature: Float,
    val status: ThermalStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val isCharging: Boolean = false,
    val batteryLevel: Int = 0,
    val thermalApiStatus: String = "Normal",
    val source: String = "Battery"
)

enum class ThermalScore(val label: String, val color: Color) {
    EXCELLENT("Excellent", Color(0xFF00E676)),
    GOOD("Good", Color(0xFF8BC34A)),
    AVERAGE("Average", Color(0xFFFFC107)),
    POOR("Poor", Color(0xFFFF9800)),
    CRITICAL("Critical", Color(0xFFF44336))
}

data class ThermalInsight(
    val title: String,
    val description: String,
    val type: InsightType
)

enum class InsightType {
    INFO, WARNING, SUCCESS, ALERT
}

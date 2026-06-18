package com.example.netpulse.widget

import kotlinx.serialization.Serializable

@Serializable
data class WidgetData(
    val downloadMbps: Double = 0.0,
    val uploadMbps: Double = 0.0,
    val pingMs: Int = 0,
    val jitterMs: Int = 0,
    val networkType: String = "WiFi",
    val isp: String = "Jio",
    val lastTestedLabel: String = "",
    val state: WidgetState = WidgetState.NO_DATA
)

enum class WidgetState {
    NO_DATA, HAS_DATA, LOADING, ERROR
}

package com.example.netpulse.data

data class SpeedResult(
    val id: Long = 0,
    val timestamp: String,        // "17 Jun 2026 · 2:34 PM"
    val dateLabel: String,        // "Today, 2:34 PM"
    val downloadMbps: Double,
    val uploadMbps: Double,
    val pingMs: Int,
    val jitterMs: Int,
    val networkType: String,      // "WiFi", "5G", "4G"
    val isp: String,              // "Jio"
    val location: String,         // "Mumbai"
    val ipAddress: String
)

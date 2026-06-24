package com.example.netpulse.insights.dailyreport

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_reports")
data class DailyReportEntity(
    @PrimaryKey val date: String, // Format: YYYY-MM-DD
    val bestSpeed: Double,
    val worstSpeed: Double,
    val averageSpeed: Double,
    val totalTests: Int,
    val bestHour: Int,
    val worstHour: Int,
    val timestamp: Long
)

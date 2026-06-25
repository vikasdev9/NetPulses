package com.example.netpulse.data.analytics

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speed_test_results")
data class SpeedTestResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val downloadMbps: Float,
    val uploadMbps: Float,
    val pingMs: Int,
    val jitterMs: Float,
    val timestamp: Long,
    val networkType: String = ""
)

@Entity(tableName = "isp_stats")
data class ISPEntity(
    @PrimaryKey val name: String,
    val avgDownload: Float,
    val avgUpload: Float,
    val avgPing: Float,
    val testCount: Int
)

@Entity(tableName = "wifi_stability")
data class WifiStabilityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val pingMs: Int,
    val rssi: Int,
    val networkType: String,
    val isConnected: Boolean
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val unlockedDate: Long? = null
)

@Entity(tableName = "daily_reports")
data class DailyReportEntity(
    @PrimaryKey val date: Long,
    val bestSpeed: Float,
    val worstSpeed: Float,
    val averageSpeed: Float,
    val totalTests: Int,
    val bestHour: Int,
    val worstHour: Int
)

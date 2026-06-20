package com.example.netpulse.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speed_results")
data class SpeedResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val downloadMbps: Double,
    val uploadMbps: Double,
    val pingMs: Int,
    val jitterMs: Int,
    val networkType: String,
    val isp: String,
    val ipAddress: String,
    val location: String,
    val serverUsed: String = "Cloudflare"
)

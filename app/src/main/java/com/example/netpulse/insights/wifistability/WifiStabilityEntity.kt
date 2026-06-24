package com.example.netpulse.insights.wifistability

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wifi_stability")
data class WifiStabilityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val pingMs: Double,
    val rssi: Int,
    val networkType: String,
    val isConnected: Boolean
)

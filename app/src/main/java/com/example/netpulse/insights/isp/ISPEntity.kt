package com.example.netpulse.insights.isp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "isp_stats")
data class ISPEntity(
    @PrimaryKey val name: String,
    val avgDownload: Double,
    val avgUpload: Double,
    val avgPing: Double,
    val testCount: Int,
    val testsAboveHalfAdvertised: Int
)

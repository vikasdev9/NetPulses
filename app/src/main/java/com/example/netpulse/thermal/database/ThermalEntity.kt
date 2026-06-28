package com.example.netpulse.thermal.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "thermal_history")
data class ThermalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val temperature: Float,
    val status: String,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val timestamp: Long,
    val notificationFired: Boolean = false,
    val source: String = "Background"
)

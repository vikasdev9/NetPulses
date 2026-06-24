package com.example.netpulse.insights.achievements

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val unlockDate: Long? = null,
    val iconResId: Int? = null // Using R.drawable.xxx
)

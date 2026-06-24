package com.example.netpulse.insights.wifistability

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WifiStabilityDao {
    @Query("SELECT * FROM wifi_stability ORDER BY timestamp DESC")
    fun getAll(): Flow<List<WifiStabilityEntity>>

    @Insert
    suspend fun insert(stability: WifiStabilityEntity)

    @Query("SELECT * FROM wifi_stability WHERE timestamp >= :startTime")
    fun getRecent(startTime: Long): Flow<List<WifiStabilityEntity>>
}

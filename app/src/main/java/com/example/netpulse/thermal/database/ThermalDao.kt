package com.example.netpulse.thermal.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ThermalDao {
    @Query("SELECT * FROM thermal_history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ThermalEntity>>

    @Query("SELECT * FROM thermal_history WHERE timestamp >= :startTime ORDER BY timestamp ASC")
    fun getHistoryFrom(startTime: Long): Flow<List<ThermalEntity>>

    @Insert
    suspend fun insert(entity: ThermalEntity)

    @Query("DELETE FROM thermal_history WHERE timestamp < :threshold")
    suspend fun deleteOldHistory(threshold: Long)

    @Query("SELECT MAX(temperature) FROM thermal_history WHERE timestamp >= :startOfDay")
    suspend fun getMaxTempToday(startOfDay: Long): Float?

    @Query("SELECT MIN(temperature) FROM thermal_history WHERE timestamp >= :startOfDay")
    suspend fun getMinTempToday(startOfDay: Long): Float?

    @Query("SELECT AVG(temperature) FROM thermal_history WHERE timestamp >= :startOfDay")
    suspend fun getAvgTempToday(startOfDay: Long): Float?
}

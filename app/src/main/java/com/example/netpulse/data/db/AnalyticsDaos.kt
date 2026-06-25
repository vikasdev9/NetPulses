package com.example.netpulse.data.db

import androidx.room.*
import com.example.netpulse.data.analytics.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedTestDao {
    @Query("SELECT * FROM speed_test_results WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getResultsForToday(startOfDay: Long): Flow<List<SpeedTestResultEntity>>

    @Query("SELECT * FROM speed_test_results WHERE timestamp >= :start AND timestamp <= :end")
    suspend fun getResultsForDateRange(start: Long, end: Long): List<SpeedTestResultEntity>

    @Query("SELECT * FROM speed_test_results ORDER BY timestamp DESC")
    fun getAll(): Flow<List<SpeedTestResultEntity>>

    @Insert
    suspend fun insert(result: SpeedTestResultEntity)

    @Delete
    suspend fun delete(result: SpeedTestResultEntity)
}

@Dao
interface ISPDao {
    @Query("SELECT * FROM isp_stats WHERE name = :name")
    suspend fun getByName(name: String): ISPEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(isp: ISPEntity)

    @Query("SELECT * FROM isp_stats")
    suspend fun getAll(): List<ISPEntity>
}

@Dao
interface WifiStabilityDao {
    @Query("SELECT * FROM wifi_stability WHERE timestamp >= :startOfDay")
    fun getForToday(startOfDay: Long): Flow<List<WifiStabilityEntity>>

    @Insert
    suspend fun insert(entity: WifiStabilityEntity)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAll(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: AchievementEntity)
}

@Dao
interface DailyReportDao {
    @Query("SELECT * FROM daily_reports WHERE date = :date")
    suspend fun getForDate(date: Long): DailyReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DailyReportEntity)
}

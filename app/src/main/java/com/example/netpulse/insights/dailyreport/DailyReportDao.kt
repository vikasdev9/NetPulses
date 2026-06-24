package com.example.netpulse.insights.dailyreport

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyReportDao {
    @Query("SELECT * FROM daily_reports ORDER BY timestamp DESC")
    fun getAll(): Flow<List<DailyReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: DailyReportEntity)

    @Query("SELECT * FROM daily_reports WHERE date = :date")
    suspend fun getByDate(date: String): DailyReportEntity?
}

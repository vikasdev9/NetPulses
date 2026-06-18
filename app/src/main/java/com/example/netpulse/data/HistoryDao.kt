package com.example.netpulse.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM speed_results ORDER BY id DESC")
    fun getAllHistory(): Flow<List<SpeedResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: SpeedResult)

    @Delete
    suspend fun deleteResult(result: SpeedResult)

    @Query("DELETE FROM speed_results")
    suspend fun deleteAll()
}

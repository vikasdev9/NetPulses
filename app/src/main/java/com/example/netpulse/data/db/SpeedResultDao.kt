package com.example.netpulse.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.netpulse.data.SpeedResult
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedResultDao {

    @Query("SELECT * FROM speed_results ORDER BY timestamp DESC")
    fun getAll(): Flow<List<SpeedResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: SpeedResult)

    @Query("DELETE FROM speed_results WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM speed_results")
    suspend fun deleteAll()
}

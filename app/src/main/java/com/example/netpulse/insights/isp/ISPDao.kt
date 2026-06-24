package com.example.netpulse.insights.isp

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ISPDao {
    @Query("SELECT * FROM isp_stats")
    fun getAll(): Flow<List<ISPEntity>>

    @Query("SELECT * FROM isp_stats WHERE name = :name")
    suspend fun getByName(name: String): ISPEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(isp: ISPEntity)
}

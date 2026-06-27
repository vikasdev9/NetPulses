package com.example.netpulse.data.wifi.db

import androidx.room.*
import com.example.netpulse.data.wifi.WifiHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WifiDao {
    @Query("SELECT * FROM wifi_history ORDER BY lastSeen DESC")
    fun getAllHistory(): Flow<List<WifiHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entity: WifiHistoryEntity)

    @Query("UPDATE wifi_history SET isFavorite = :isFavorite WHERE bssid = :bssid")
    suspend fun updateFavorite(bssid: String, isFavorite: Boolean)

    @Query("DELETE FROM wifi_history")
    suspend fun clearHistory()
}

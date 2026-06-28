package com.example.netpulse.data.lan.db

import androidx.room.*
import com.example.netpulse.data.lan.LanDeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LanDao {
    @Query("SELECT * FROM lan_device_history ORDER BY lastSeen DESC")
    fun getAllHistory(): Flow<List<LanDeviceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: LanDeviceEntity)

    @Query("UPDATE lan_device_history SET nickname = :nickname WHERE ipAddress = :ip")
    suspend fun updateNickname(ip: String, nickname: String?)

    @Query("UPDATE lan_device_history SET notes = :notes WHERE ipAddress = :ip")
    suspend fun updateNotes(ip: String, notes: String?)

    @Query("UPDATE lan_device_history SET isFavorite = :isFavorite WHERE ipAddress = :ip")
    suspend fun updateFavorite(ip: String, isFavorite: Boolean)

    @Query("DELETE FROM lan_device_history")
    suspend fun clearHistory()
}

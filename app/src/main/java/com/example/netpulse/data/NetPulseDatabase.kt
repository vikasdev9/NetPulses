package com.example.netpulse.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.netpulse.data.analytics.*
import com.example.netpulse.data.db.*
import com.example.netpulse.data.wifi.WifiHistoryEntity
import com.example.netpulse.data.wifi.db.WifiDao
import com.example.netpulse.data.lan.LanDeviceEntity
import com.example.netpulse.data.lan.db.LanDao

@Database(
    entities = [
        SpeedResult::class,
        SpeedTestResultEntity::class,
        ISPEntity::class,
        WifiStabilityEntity::class,
        AchievementEntity::class,
        DailyReportEntity::class,
        WifiHistoryEntity::class,
        LanDeviceEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class NetPulseDatabase : RoomDatabase() {
    abstract fun speedResultDao(): SpeedResultDao
    abstract fun speedTestDao(): SpeedTestDao
    abstract fun ispDao(): ISPDao
    abstract fun wifiStabilityDao(): WifiStabilityDao
    abstract fun achievementDao(): AchievementDao
    abstract fun dailyReportDao(): DailyReportDao
    abstract fun wifiDao(): WifiDao
    abstract fun lanDao(): LanDao

    companion object {
        @Volatile
        private var INSTANCE: NetPulseDatabase? = null

        fun getDatabase(context: Context): NetPulseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NetPulseDatabase::class.java,
                    "netpulse_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

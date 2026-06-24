package com.example.netpulse.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.netpulse.data.db.SpeedResultDao
import com.example.netpulse.insights.achievements.AchievementDao
import com.example.netpulse.insights.achievements.AchievementEntity
import com.example.netpulse.insights.dailyreport.DailyReportDao
import com.example.netpulse.insights.dailyreport.DailyReportEntity
import com.example.netpulse.insights.isp.ISPDao
import com.example.netpulse.insights.isp.ISPEntity
import com.example.netpulse.insights.wifistability.WifiStabilityDao
import com.example.netpulse.insights.wifistability.WifiStabilityEntity

@Database(
    entities = [
        SpeedResult::class,
        DailyReportEntity::class,
        ISPEntity::class,
        AchievementEntity::class,
        WifiStabilityEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class NetPulseDatabase : RoomDatabase() {
    abstract fun speedResultDao(): SpeedResultDao
    abstract fun dailyReportDao(): DailyReportDao
    abstract fun ispDao(): ISPDao
    abstract fun achievementDao(): AchievementDao
    abstract fun wifiStabilityDao(): WifiStabilityDao

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

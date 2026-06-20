package com.example.netpulse.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.netpulse.data.db.SpeedResultDao

@Database(entities = [SpeedResult::class], version = 2, exportSchema = false)
abstract class NetPulseDatabase : RoomDatabase() {
    abstract fun speedResultDao(): SpeedResultDao

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

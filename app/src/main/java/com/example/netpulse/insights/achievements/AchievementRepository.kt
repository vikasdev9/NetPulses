package com.example.netpulse.insights.achievements

import android.content.Context
import com.example.netpulse.NetPulseApplication
import com.example.netpulse.data.SpeedResult
import com.example.netpulse.data.analytics.AchievementEntity
import kotlinx.coroutines.flow.first
import java.util.*

class AchievementRepository(private val context: Context) {
    private val dao = (context.applicationContext as NetPulseApplication).database.achievementDao()
    private val speedDao = (context.applicationContext as NetPulseApplication).database.speedResultDao()

    suspend fun checkAchievements(latest: SpeedResult) {
        val allResults = speedDao.getAll().first()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = latest.timestamp
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        // 1. First Test
        unlock("first_test", "First Test", "Run your first speed test")

        // 2. Speed Demon
        if (latest.downloadMbps > 100) {
            unlock("speed_demon", "Speed Demon", "Record download > 100 Mbps")
        }

        // 3. Low Latency
        if (latest.pingMs < 10) {
            unlock("low_latency", "Low Latency", "Ping under 10ms")
        }

        // 4. Night Owl (0-4 AM)
        if (hour in 0..4) {
            unlock("night_owl", "Night Owl", "Run test between 12AM-4AM")
        }

        // 5. Early Bird (5-7 AM)
        if (hour in 5..7) {
            unlock("early_bird", "Early Bird", "Run test between 5AM-7AM")
        }

        // 6. Streaks
        val streak = calculateStreak(allResults)
        if (streak >= 3) unlock("streak_3", "3 Day Streak", "Test 3 consecutive days")
        if (streak >= 7) unlock("streak_7", "7 Day Streak", "Test 7 consecutive days")
        if (streak >= 30) unlock("streak_30", "30 Day Streak", "Test 30 consecutive days")
    }

    private suspend fun unlock(id: String, title: String, desc: String) {
        dao.insertOrUpdate(AchievementEntity(id, title, desc, true, System.currentTimeMillis()))
    }

    fun calculateStreak(results: List<SpeedResult>): Int {
        if (results.isEmpty()) return 0
        val days = results.map { 
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.timestamp
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.distinct().sortedDescending()

        var streak = 1
        val oneDayMs = 24 * 60 * 60 * 1000L
        
        // Check if latest test was today or yesterday
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (days.first() < today - oneDayMs) return 0

        for (i in 0 until days.size - 1) {
            if (days[i] - days[i+1] == oneDayMs) {
                streak++
            } else break
        }
        return streak
    }
}

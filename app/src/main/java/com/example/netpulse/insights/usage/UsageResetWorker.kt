package com.example.netpulse.insights.usage

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.netpulse.data.datastore.UserPreferences
import kotlinx.coroutines.flow.first
import java.util.Calendar

class UsageResetWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userPreferences = UserPreferences(applicationContext)
        userPreferences.resetUsageToday()
        
        // Reset week on Sunday
        val calendar = Calendar.getInstance()
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            // Need to add resetUsageWeek to UserPreferences if we want this
        }
        
        return Result.success()
    }
}

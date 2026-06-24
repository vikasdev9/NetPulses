package com.example.netpulse.insights.dailyreport

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.netpulse.NetPulseApplication
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class DailyReportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as NetPulseApplication
        val speedDao = app.database.speedResultDao()
        val reportDao = app.database.dailyReportDao()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        val startOfDay = calendar.timeInMillis
        val endOfDay = System.currentTimeMillis()

        val todayResults = speedDao.getAll().first().filter { it.timestamp in startOfDay..endOfDay }

        if (todayResults.isNotEmpty()) {
            val best = todayResults.maxBy { it.downloadMbps }
            val worst = todayResults.minBy { it.downloadMbps }
            val avg = todayResults.map { it.downloadMbps }.average()
            
            // Find best/worst hour
            val hourMap = todayResults.groupBy { 
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.timestamp
                cal.get(Calendar.HOUR_OF_DAY)
            }.mapValues { it.value.map { r -> r.downloadMbps }.average() }
            
            val bestHour = hourMap.maxByOrNull { it.value }?.key ?: 0
            val worstHour = hourMap.minByOrNull { it.value }?.key ?: 0

            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            val report = DailyReportEntity(
                date = dateStr,
                bestSpeed = best.downloadMbps,
                worstSpeed = worst.downloadMbps,
                averageSpeed = avg,
                totalTests = todayResults.size,
                bestHour = bestHour,
                worstHour = worstHour,
                timestamp = System.currentTimeMillis()
            )
            
            reportDao.insert(report)
        }

        return Result.success()
    }
}

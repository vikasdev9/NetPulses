package com.example.netpulse.data.analytics

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process
import android.provider.Settings
import android.text.format.DateUtils
import java.util.*

class ScreenTimeHelper(private val context: Context) {

    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager

    fun hasUsagePermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun getTodayTotalScreenTime(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
        return stats.values.sumOf { it.totalTimeInForeground }
    }

    fun getPerAppScreenTime(startTime: Long, endTime: Long): List<AppScreenTime> {
        val stats = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
        val list = mutableListOf<AppScreenTime>()

        for (usageStat in stats.values) {
            if (usageStat.totalTimeInForeground < 60000) continue // Skip less than 1 min

            try {
                val appInfo = packageManager.getApplicationInfo(usageStat.packageName, 0)
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                val lastUsed = usageStat.lastTimeUsed
                val lastUsedLabel = if (lastUsed > 0) {
                    DateUtils.getRelativeTimeSpanString(lastUsed, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()
                } else "Never"

                list.add(
                    AppScreenTime(
                        packageName = usageStat.packageName,
                        appName = appName,
                        appIcon = null, // Load in UI/ViewModel
                        totalTimeMs = usageStat.totalTimeInForeground,
                        lastUsedMs = lastUsed,
                        lastUsedLabel = lastUsedLabel,
                        formattedTime = formatDuration(usageStat.totalTimeInForeground)
                    )
                )
            } catch (e: PackageManager.NameNotFoundException) {
                // App uninstalled or system app hidden
            }
        }

        return list.sortedByDescending { it.totalTimeMs }
    }

    fun getWeeklyScreenTime(): List<DailyScreenTime> {
        val list = mutableListOf<DailyScreenTime>()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        for (i in 0 until 7) {
            val startTime = calendar.timeInMillis
            val endTime = startTime + (24 * 60 * 60 * 1000) - 1
            
            val stats = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
            val totalToday = stats.values.sumOf { it.totalTimeInForeground }
            
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val label = if (i == 0) "Today" else if (i == 1) "Yesterday" else dayLabels[dayOfWeek - 1]

            list.add(0, DailyScreenTime(
                date = calendar.time,
                dayLabel = label,
                totalMs = totalToday,
                formatted = formatDuration(totalToday)
            ))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return list
    }
}

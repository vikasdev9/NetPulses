package com.example.netpulse.data.analytics

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.util.Log
import java.util.*

class DataUsageHelper(private val context: Context) {

    private val networkStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
    private val packageManager = context.packageManager

    fun getTodayMobileData(): DataUsage {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        return getNetworkData(ConnectivityManager.TYPE_MOBILE, null, startTime, endTime)
    }

    fun getTodayWifiData(): DataUsage {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        return getNetworkData(ConnectivityManager.TYPE_WIFI, null, startTime, endTime)
    }

    private fun getNetworkData(networkType: Int, subscriberId: String?, startTime: Long, endTime: Long): DataUsage {
        var rxBytes = 0L
        var txBytes = 0L

        try {
            val bucket = networkStatsManager.querySummaryForDevice(networkType, subscriberId, startTime, endTime)
            rxBytes = bucket.rxBytes
            txBytes = bucket.txBytes
        } catch (e: Exception) {
            Log.e("DataUsageHelper", "Error querying device summary", e)
        }

        return DataUsage(rxBytes, txBytes)
    }

    fun getPerAppDataUsage(startTime: Long, endTime: Long): List<AppDataUsage> {
        val appDataList = mutableListOf<AppDataUsage>()
        
        // 1. Get all stats for the period in one go (very fast)
        val wifiStats = networkStatsManager.querySummary(ConnectivityManager.TYPE_WIFI, null, startTime, endTime)
        val mobileStats = networkStatsManager.querySummary(ConnectivityManager.TYPE_MOBILE, null, startTime, endTime)
        
        val usageMap = mutableMapOf<Int, Pair<Long, Long>>() // uid -> <rx, tx>

        val bucket = NetworkStats.Bucket()
        
        // Process WiFi
        while (wifiStats.hasNextBucket()) {
            wifiStats.getNextBucket(bucket)
            val current = usageMap.getOrDefault(bucket.uid, Pair(0L, 0L))
            usageMap[bucket.uid] = Pair(current.first + bucket.rxBytes, current.second + bucket.txBytes)
        }
        wifiStats.close()

        // Process Mobile
        while (mobileStats.hasNextBucket()) {
            mobileStats.getNextBucket(bucket)
            val current = usageMap.getOrDefault(bucket.uid, Pair(0L, 0L))
            usageMap[bucket.uid] = Pair(current.first + bucket.rxBytes, current.second + bucket.txBytes)
        }
        mobileStats.close()

        // 2. Map UIDs to actual apps (only for apps that have usage)
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val appsByUid = installedApps.associateBy { it.uid }

        for ((uid, usage) in usageMap) {
            if (uid < 10000) continue // Skip system
            
            val appInfo = appsByUid[uid] ?: continue
            val totalRx = usage.first
            val totalTx = usage.second

            if (totalRx + totalTx > 1024 * 10) { // > 10KB to ignore noise
                appDataList.add(
                    AppDataUsage(
                        uid = uid,
                        packageName = appInfo.packageName,
                        appName = packageManager.getApplicationLabel(appInfo).toString(),
                        appIcon = null,
                        rxBytes = totalRx,
                        txBytes = totalTx
                    )
                )
            }
        }

        return appDataList.sortedByDescending { it.rxBytes + it.txBytes }
    }

    fun getWeeklyDailyUsage(): List<DailyUsage> {
        val dailyUsageList = mutableListOf<DailyUsage>()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        for (i in 0 until 7) {
            val endTime = calendar.timeInMillis + (24 * 60 * 60 * 1000) - 1
            val startTime = calendar.timeInMillis
            
            val mobile = getNetworkData(ConnectivityManager.TYPE_MOBILE, null, startTime, endTime)
            val wifi = getNetworkData(ConnectivityManager.TYPE_WIFI, null, startTime, endTime)
            
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val label = if (i == 0) "Today" else if (i == 1) "Yesterday" else dayLabels[dayOfWeek - 1]

            dailyUsageList.add(0, DailyUsage(
                date = calendar.time,
                dayLabel = label,
                mobileRx = mobile.rxBytes,
                mobileTx = mobile.txBytes,
                wifiRx = wifi.rxBytes,
                wifiTx = wifi.txBytes
            ))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return dailyUsageList
    }
}

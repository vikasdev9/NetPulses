package com.example.netpulse.data.analytics

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Process
import android.os.RemoteException
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
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        for (app in installedApps) {
            if (app.uid < 10000) continue // Skip system apps

            val wifiData = getAppNetworkData(ConnectivityManager.TYPE_WIFI, app.uid, startTime, endTime)
            val mobileData = getAppNetworkData(ConnectivityManager.TYPE_MOBILE, app.uid, startTime, endTime)

            val totalRx = wifiData.rxBytes + mobileData.rxBytes
            val totalTx = wifiData.txBytes + mobileData.txBytes

            if (totalRx + totalTx > 0) {
                appDataList.add(
                    AppDataUsage(
                        uid = app.uid,
                        packageName = app.packageName,
                        appName = packageManager.getApplicationLabel(app).toString(),
                        appIcon = null, // Will load icon in UI or ViewModel to avoid heavy objects here
                        rxBytes = totalRx,
                        txBytes = totalTx
                    )
                )
            }
        }
        return appDataList.sortedByDescending { it.rxBytes + it.txBytes }
    }

    private fun getAppNetworkData(networkType: Int, uid: Int, startTime: Long, endTime: Long): DataUsage {
        var rxBytes = 0L
        var txBytes = 0L
        try {
            val stats = networkStatsManager.queryDetailsForUid(networkType, null, startTime, endTime, uid)
            val bucket = NetworkStats.Bucket()
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                rxBytes += bucket.rxBytes
                txBytes += bucket.txBytes
            }
            stats.close()
        } catch (e: Exception) {
            // Log.e("DataUsageHelper", "Error querying app details for uid $uid", e)
        }
        return DataUsage(rxBytes, txBytes)
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

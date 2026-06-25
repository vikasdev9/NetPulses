package com.example.netpulse.insights.isp

import android.content.Context
import com.example.netpulse.NetPulseApplication
import com.example.netpulse.data.analytics.ISPEntity
import com.example.netpulse.ui.viewmodel.IspPerformance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class ISPRepository(private val context: Context) {
    private val dao = (context.applicationContext as NetPulseApplication).database.ispDao()
    private val client = OkHttpClient()

    suspend fun fetchIspName(): String = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url("https://ipapi.co/json/").build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext "Unknown"
                val json = JSONObject(response.body?.string() ?: "")
                json.optString("org", "Unknown").substringAfter(" ")
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    suspend fun updateStats(name: String, download: Float, upload: Float, ping: Float) {
        val existing = dao.getByName(name)
        if (existing == null) {
            dao.insertOrUpdate(ISPEntity(name, download, upload, ping, 1))
        } else {
            val count = existing.testCount + 1
            val newAvgDown = (existing.avgDownload * existing.testCount + download) / count
            val newAvgUp = (existing.avgUpload * existing.testCount + upload) / count
            val newAvgPing = (existing.avgPing * existing.testCount + ping) / count
            dao.insertOrUpdate(ISPEntity(name, newAvgDown, newAvgUp, newAvgPing, count))
        }
    }

    suspend fun getPerformance(name: String, advertised: Float): IspPerformance {
        val stats = dao.getByName(name) ?: return IspPerformance(advertised = advertised)
        
        val deliveryScore = (stats.avgDownload / advertised * 100).toInt().coerceIn(0, 100)
        
        // Reliability: % of tests above 50% of advertised (Mocked for now as we don't store individual tests in ISPEntity)
        val reliability = if (deliveryScore > 50) 90 else 60 
        
        val rank = when {
            deliveryScore > 90 -> "Top 10%"
            deliveryScore > 70 -> "Average"
            else -> "Below Average"
        }

        return IspPerformance(
            deliveryScore = deliveryScore,
            reliabilityScore = reliability,
            rankBadge = rank,
            actualAvg = stats.avgDownload,
            advertised = advertised
        )
    }
}

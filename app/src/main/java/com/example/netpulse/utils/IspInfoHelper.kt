package com.example.netpulse.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class IspInfoResult(
    val ip: String = "—",
    val name: String = "—",
    val asn: String = "—",
    val org: String = "—",
    val country: String = "—",
    val region: String = "—",
    val city: String = "—",
    val timezone: String = "—"
)

object IspInfoHelper {
    private val client = OkHttpClient()

    suspend fun fetchDetailedIspInfo(): IspInfoResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://ipinfo.io/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext IspInfoResult()
                
                val json = JSONObject(response.body?.string() ?: "")
                val rawIp = json.optString("ip", "—")
                val rawOrg = json.optString("org", "—")

                // Mask last octet for privacy: "103.24.45.67" → "103.24.45.xx"
                val maskedIp = if (rawIp.contains(".")) {
                    rawIp.substringBeforeLast(".") + ".xx"
                } else rawIp

                // Clean Org: "AS55836 Reliance Jio" → "Reliance Jio"
                val cleanedOrg = if (rawOrg.startsWith("AS", ignoreCase = true)) {
                    rawOrg.substringAfter(" ").trim()
                } else rawOrg

                IspInfoResult(
                    ip = maskedIp,
                    name = cleanedOrg,
                    asn = rawOrg.substringBefore(" ", "—"),
                    org = rawOrg,
                    country = json.optString("country", "—"),
                    region = json.optString("region", "—"),
                    city = json.optString("city", "—"),
                    timezone = json.optString("timezone", "—")
                )
            }
        } catch (e: Exception) {
            IspInfoResult()
        }
    }

    // Legacy support for basic IspInfo
    suspend fun fetchIspInfo(context: Context): IspInfo {
        val detailed = fetchDetailedIspInfo()
        return IspInfo(ip = detailed.ip, isp = detailed.name)
    }
}

data class IspInfo(
    val ip: String = "—",
    val isp: String = "—"
)

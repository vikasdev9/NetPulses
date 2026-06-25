package com.example.netpulse.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.text.format.Formatter
import com.example.netpulse.NetPulseApplication
import com.example.netpulse.data.SpeedResult
import com.example.netpulse.ui.viewmodel.*
import com.example.netpulse.utils.IspInfoHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.net.InetAddress
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.*

class NetworkRepository(private val context: Context) {

    private val dao = (context.applicationContext as NetPulseApplication).database.speedResultDao()

    fun getNetworkStatus(): Flow<NetworkStatus> = flow {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        
        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)
        
        val wifiInfo = try {
            wifi.connectionInfo
        } catch (e: SecurityException) {
            null
        }

        val isConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val type = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Mobile"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
            else -> "None"
        }

        emit(NetworkStatus(
            isConnected = isConnected,
            type = type,
            ssid = if (type == "WiFi" && wifiInfo != null) wifiInfo.ssid.removeSurrounding("\"") else "—",
            signalStrength = if (type == "WiFi" && wifiInfo != null) WifiManager.calculateSignalLevel(wifiInfo.rssi, 100) else 0,
            rssi = wifiInfo?.rssi ?: 0,
            frequency = if (type == "WiFi" && wifiInfo != null) "${wifiInfo.frequency} MHz" else "—",
            linkSpeed = wifiInfo?.linkSpeed ?: 0,
            txSpeed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) wifiInfo?.txLinkSpeedMbps ?: 0 else 0,
            rxSpeed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) wifiInfo?.rxLinkSpeedMbps ?: 0 else 0
        ))
    }

    fun getInternetDetails(): Flow<InternetDetails> = flow {
        val ispDetailed = IspInfoHelper.fetchDetailedIspInfo()
        val localIp = getLocalIpAddress()
        
        emit(InternetDetails(
            publicIp = ispDetailed.ip,
            localIp = localIp ?: "—",
            interfaceName = "wlan0" // Simplified
        ))
    }

    fun getIspInfo(): Flow<IspInfo> = flow {
        val detailed = IspInfoHelper.fetchDetailedIspInfo()
        emit(IspInfo(
            name = detailed.name,
            asn = detailed.asn,
            org = detailed.org,
            country = detailed.country,
            region = detailed.region,
            city = detailed.city,
            timezone = detailed.timezone
        ))
    }

    fun getSpeedSummary(): Flow<SpeedSummary> = flow {
        val latest = dao.getAll().first().firstOrNull()
        if (latest != null) {
            emit(SpeedSummary(
                download = latest.downloadMbps.toFloat(),
                upload = latest.uploadMbps.toFloat(),
                ping = latest.pingMs,
                jitter = latest.jitterMs,
                testTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(latest.timestamp)),
                server = latest.serverUsed
            ))
        } else {
            emit(SpeedSummary())
        }
    }

    fun getDataUsage(): Flow<NetworkDataUsage> = flow {
        // Real data usage requires NetworkStatsManager and special permissions.
        // For now, we return empty usage or mocked zeros.
        emit(NetworkDataUsage())
    }

    fun getDeviceInfo(): Flow<DeviceInfo> = flow {
        val displayMetrics = context.resources.displayMetrics
        val totalStorage = context.filesDir.totalSpace / (1024 * 1024 * 1024)
        
        emit(DeviceInfo(
            androidVersion = "Android ${Build.VERSION.RELEASE}",
            sdk = Build.VERSION.SDK_INT,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            resolution = "${displayMetrics.widthPixels} x ${displayMetrics.heightPixels}",
            cpuAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "—",
            storage = "${totalStorage} GB"
        ))
    }

    fun getTimeline(): Flow<List<TimelineEvent>> = flow {
        val results = dao.getAll().first().take(5)
        val events = results.map {
            TimelineEvent(
                time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it.timestamp)),
                title = "Test Finished",
                description = "Download: ${"%.1f".format(it.downloadMbps)} Mbps",
                type = TimelineType.TEST_FINISHED
            )
        }
        emit(events)
    }

    fun getDiagnostics(): Flow<AdvancedDiagnostics> = flow {
        emit(AdvancedDiagnostics())
    }

    fun getSecurityStatus(): Flow<SecurityStatus> = flow {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)

        emit(SecurityStatus(
            vpnActive = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true,
            metered = cm.isActiveNetworkMetered,
            roaming = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING) == false,
            validated = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        ))
    }

    fun calculateHealthScore(download: Float, upload: Float, ping: Int, jitter: Float): Int {
        val idealDown = 100f
        val idealUp = 50f
        val idealPing = 10f
        val idealJitter = 1f

        val downScore = (download / idealDown).coerceIn(0f, 1f) * 0.40f
        val upScore = (upload / idealUp).coerceIn(0f, 1f) * 0.20f
        val pingScore = (idealPing / ping.coerceAtLeast(1)).coerceIn(0f, 1f) * 0.25f
        val jitterScore = (idealJitter / jitter.coerceAtLeast(0.1f)).coerceIn(0f, 1f) * 0.15f

        return ((downScore + upScore + pingScore + jitterScore) * 100).toInt()
    }

    fun getRecommendations(): Flow<List<RecommendationItem>> = flow {
        val latest = dao.getAll().first().firstOrNull()
        val recs = mutableListOf<RecommendationItem>()
        
        if (latest != null) {
            val down = latest.downloadMbps.toFloat()
            val up = latest.uploadMbps.toFloat()
            val ping = latest.pingMs
            val jitter = latest.jitterMs.toFloat()

            // 1. Evening speed (Mocked comparison for now)
            // 2. High latency
            if (ping > 50) {
                recs.add(RecommendationItem("lat", "High latency detected", "Try connecting via ethernet or restart your router.", RecommendationPriority.HIGH))
            }
            // 3. Jitter
            if (jitter > 10) {
                recs.add(RecommendationItem("jit", "Unstable connection", "Avoid video calls during peak hours.", RecommendationPriority.MEDIUM))
            }
            // 4. Actual < 50% Advertised (Assume 100 Mbps)
            if (down < 50) {
                recs.add(RecommendationItem("isp", "Low ISP performance", "Your ISP is delivering less than 50% of your plan speed.", RecommendationPriority.HIGH))
            }
            // 5. Upload < 10% Download
            if (up < down * 0.1f) {
                recs.add(RecommendationItem("up", "Low upload speed", "This affects video calls and cloud backup performance.", RecommendationPriority.MEDIUM))
            }
            
            val score = calculateHealthScore(down, up, ping, jitter)
            if (score < 50) {
                recs.add(RecommendationItem("health", "Poor network health", "Try restarting your router and modem.", RecommendationPriority.HIGH))
            }
        }
        
        if (recs.isEmpty()) {
            recs.add(RecommendationItem("all_clear", "All clear", "Network performing normally.", RecommendationPriority.LOW))
        }

        emit(recs)
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) { }
        return null
    }
}

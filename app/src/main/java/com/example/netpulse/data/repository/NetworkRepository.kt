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

    fun getDataUsage(): Flow<DataUsage> = flow {
        // Real data usage requires NetworkStatsManager and special permissions.
        // For now, we return empty usage or mocked zeros.
        emit(DataUsage())
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

    fun getRecommendations(): Flow<List<String>> = flow {
        val latest = dao.getAll().first().firstOrNull()
        val recs = mutableListOf<String>()
        
        if (latest != null) {
            if (latest.downloadMbps < 25) recs.add("Download speed is low for 4K streaming.")
            if (latest.pingMs > 50) recs.add("High latency detected. Gaming might be affected.")
        } else {
            recs.add("Perform a speed test to get recommendations.")
        }
        
        recs.add("Network performing normally.")
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

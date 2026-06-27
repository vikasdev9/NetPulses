package com.example.netpulse.widget

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.netpulse.data.SpeedResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "widget_prefs")

enum class WidgetState {
    NO_DATA, HAS_DATA, LOADING, ERROR
}

data class WidgetData(
    val downloadMbps: Double = 0.0,
    val uploadMbps: Double = 0.0,
    val pingMs: Int = 0,
    val jitterMs: Int = 0,
    val packetLoss: Float = 0f,
    val healthScore: Int = 0,
    val networkType: String = "—",
    val wifiName: String = "—",
    val signalStrength: Int = 0,
    val isp: String = "—",
    val publicIp: String = "—",
    val localIp: String = "—",
    val vpnStatus: Boolean = false,
    val privateDns: String = "—",
    val lastTestedLabel: String = "—",
    val gamingRating: String = "—",
    val streamingRating: String = "—",
    val trend: String = "Stable", // Up, Down, Stable
    val batteryPct: Int = 0,
    val usageToday: String = "0 MB",
    val theme: String = "System",
    val state: WidgetState = WidgetState.NO_DATA
)

object WidgetDataStore {
    private val DOWNLOAD = doublePreferencesKey("download")
    private val UPLOAD = doublePreferencesKey("upload")
    private val PING = intPreferencesKey("ping")
    private val JITTER = intPreferencesKey("jitter")
    private val LOSS = floatPreferencesKey("loss")
    private val HEALTH = intPreferencesKey("health")
    private val NETWORK = stringPreferencesKey("network")
    private val WIFI_NAME = stringPreferencesKey("wifi_name")
    private val SIGNAL = intPreferencesKey("signal")
    private val ISP = stringPreferencesKey("isp")
    private val PUBLIC_IP = stringPreferencesKey("public_ip")
    private val LOCAL_IP = stringPreferencesKey("local_ip")
    private val VPN = booleanPreferencesKey("vpn")
    private val DNS = stringPreferencesKey("dns")
    private val LABEL = stringPreferencesKey("label")
    private val GAMING = stringPreferencesKey("gaming")
    private val STREAMING = stringPreferencesKey("streaming")
    private val TREND = stringPreferencesKey("trend")
    private val BATTERY = intPreferencesKey("battery")
    private val USAGE = stringPreferencesKey("usage")
    private val THEME = stringPreferencesKey("widget_theme")
    private val STATE = stringPreferencesKey("state")

    suspend fun saveWidgetData(context: Context, result: SpeedResult) {
        context.dataStore.edit { prefs ->
            prefs[DOWNLOAD] = result.downloadMbps
            prefs[UPLOAD] = result.uploadMbps
            prefs[PING] = result.pingMs
            prefs[JITTER] = result.jitterMs
            prefs[NETWORK] = result.networkType
            prefs[ISP] = result.isp
            prefs[LABEL] = java.text.SimpleDateFormat("h:mm a", java.util.Locale.US).format(java.util.Date(result.timestamp))
            prefs[STATE] = WidgetState.HAS_DATA.name
            
            // Calculate ratings (Simplified)
            prefs[GAMING] = if (result.pingMs < 30) "Excellent" else if (result.pingMs < 60) "Good" else "Fair"
            prefs[STREAMING] = if (result.downloadMbps > 25) "4K" else if (result.downloadMbps > 10) "HD" else "SD"
            
            // Score (Simplified)
            val score = ((result.downloadMbps / 100 * 40) + (result.uploadMbps / 50 * 20) + ((100 - result.pingMs).coerceAtLeast(0) / 100.0 * 40)).toInt().coerceIn(0, 100)
            prefs[HEALTH] = score
        }
    }

    suspend fun updateNetworkInfo(
        context: Context,
        wifiName: String,
        signal: Int,
        publicIp: String,
        localIp: String,
        vpn: Boolean,
        dns: String,
        battery: Int,
        usage: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[WIFI_NAME] = wifiName
            prefs[SIGNAL] = signal
            prefs[PUBLIC_IP] = publicIp
            prefs[LOCAL_IP] = localIp
            prefs[VPN] = vpn
            prefs[DNS] = dns
            prefs[BATTERY] = battery
            prefs[USAGE] = usage
        }
    }

    suspend fun loadWidgetData(context: Context): WidgetData {
        return context.dataStore.data.map { prefs ->
            WidgetData(
                downloadMbps = prefs[DOWNLOAD] ?: 0.0,
                uploadMbps = prefs[UPLOAD] ?: 0.0,
                pingMs = prefs[PING] ?: 0,
                jitterMs = prefs[JITTER] ?: 0,
                packetLoss = prefs[LOSS] ?: 0f,
                healthScore = prefs[HEALTH] ?: 0,
                networkType = prefs[NETWORK] ?: "—",
                wifiName = prefs[WIFI_NAME] ?: "—",
                signalStrength = prefs[SIGNAL] ?: 0,
                isp = prefs[ISP] ?: "—",
                publicIp = prefs[PUBLIC_IP] ?: "—",
                localIp = prefs[LOCAL_IP] ?: "—",
                vpnStatus = prefs[VPN] ?: false,
                privateDns = prefs[DNS] ?: "—",
                lastTestedLabel = prefs[LABEL] ?: "—",
                gamingRating = prefs[GAMING] ?: "—",
                streamingRating = prefs[STREAMING] ?: "—",
                trend = prefs[TREND] ?: "Stable",
                batteryPct = prefs[BATTERY] ?: 0,
                usageToday = prefs[USAGE] ?: "0 MB",
                theme = prefs[THEME] ?: "System",
                state = WidgetState.valueOf(prefs[STATE] ?: WidgetState.NO_DATA.name)
            )
        }.first()
    }

    suspend fun getData(context: Context): WidgetData = loadWidgetData(context)

    suspend fun updateData(context: Context, data: WidgetData) {
        context.dataStore.edit { prefs ->
            prefs[DOWNLOAD] = data.downloadMbps
            prefs[UPLOAD] = data.uploadMbps
            prefs[PING] = data.pingMs
            prefs[JITTER] = data.jitterMs
            prefs[LOSS] = data.packetLoss
            prefs[HEALTH] = data.healthScore
            prefs[NETWORK] = data.networkType
            prefs[WIFI_NAME] = data.wifiName
            prefs[SIGNAL] = data.signalStrength
            prefs[ISP] = data.isp
            prefs[PUBLIC_IP] = data.publicIp
            prefs[LOCAL_IP] = data.localIp
            prefs[VPN] = data.vpnStatus
            prefs[DNS] = data.privateDns
            prefs[LABEL] = data.lastTestedLabel
            prefs[GAMING] = data.gamingRating
            prefs[STREAMING] = data.streamingRating
            prefs[TREND] = data.trend
            prefs[BATTERY] = data.batteryPct
            prefs[USAGE] = data.usageToday
            prefs[THEME] = data.theme
            prefs[STATE] = data.state.name
        }
    }
}

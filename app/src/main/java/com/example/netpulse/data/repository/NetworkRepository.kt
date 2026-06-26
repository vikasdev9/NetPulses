package com.example.netpulse.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.*
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
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Mobile"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
            else -> "Disconnected"
        }

        val frequency = wifiInfo?.frequency ?: 0
        val band = when {
            frequency in 2400..2500 -> "2.4 GHz"
            frequency in 4900..5900 -> "5 GHz"
            frequency > 5900 -> "6 GHz"
            else -> "—"
        }
        
        val wifiStandard = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && wifiInfo != null) {
            when (wifiInfo.wifiStandard) {
                ScanResult.WIFI_STANDARD_LEGACY -> "Legacy"
                ScanResult.WIFI_STANDARD_11N -> "Wi-Fi 4 (802.11n)"
                ScanResult.WIFI_STANDARD_11AC -> "Wi-Fi 5 (802.11ac)"
                ScanResult.WIFI_STANDARD_11AX -> "Wi-Fi 6 (802.11ax)"
                ScanResult.WIFI_STANDARD_11AD -> "Wi-Fi 60GHz (802.11ad)"
                else -> "—"
            }
        } else "—"

        val security = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && wifiInfo != null) {
            when (wifiInfo.currentSecurityType) {
                WifiInfo.SECURITY_TYPE_OPEN -> "Open"
                WifiInfo.SECURITY_TYPE_PSK -> "WPA2-PSK"
                WifiInfo.SECURITY_TYPE_SAE -> "WPA3-SAE"
                WifiInfo.SECURITY_TYPE_EAP -> "WPA-EAP"
                else -> "WPA2/WPA3"
            }
        } else "WPA2/WPA3"

        emit(NetworkStatus(
            isConnected = isConnected,
            type = type,
            ssid = if (type == "Wi-Fi" && wifiInfo != null) {
                val s = wifiInfo.ssid.removeSurrounding("\"")
                if (s == "<unknown ssid>") "—" else s
            } else "—",
            bssid = wifiInfo?.bssid ?: "—",
            security = security,
            signalStrength = if (type == "Wi-Fi" && wifiInfo != null) WifiManager.calculateSignalLevel(wifiInfo.rssi, 100) else 0,
            signalLevel = if (type == "Wi-Fi" && wifiInfo != null) WifiManager.calculateSignalLevel(wifiInfo.rssi, 5) else 0,
            signalPercentage = if (type == "Wi-Fi" && wifiInfo != null) {
                ((wifiInfo.rssi + 100) * 2).coerceIn(0, 100)
            } else 0,
            rssi = wifiInfo?.rssi ?: 0,
            frequency = if (type == "Wi-Fi" && frequency > 0) "$frequency MHz" else "—",
            band = band,
            channel = if (frequency > 0) {
                when {
                    frequency in 2412..2484 -> (frequency - 2412) / 5 + 1
                    frequency in 5170..5825 -> (frequency - 5170) / 5 + 34
                    else -> 0
                }
            } else 0,
            wifiStandard = wifiStandard,
            isHidden = false, // isHiddenSsid is hidden/restricted on some versions
            isRandomizedMac = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Heuristic: check if OUI is randomized
                wifiInfo?.macAddress?.let { it.startsWith("02:") || it.startsWith("06:") || it.startsWith("0a:") || it.startsWith("0e:") } ?: false
            } else false,
            linkSpeed = wifiInfo?.linkSpeed ?: 0,
            txSpeed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) wifiInfo?.txLinkSpeedMbps ?: 0 else 0,
            rxSpeed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) wifiInfo?.rxLinkSpeedMbps ?: 0 else 0
        ))
    }

    fun getInternetDetails(): Flow<InternetDetails> = flow {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork
        val linkProperties = cm.getLinkProperties(activeNetwork)
        
        val localIp = getLocalIpAddress()
        val dnsServers = linkProperties?.dnsServers?.map { it.hostAddress } ?: emptyList()
        val gateway = linkProperties?.routes?.firstOrNull { it.isDefaultRoute }?.gateway?.hostAddress
        val ipv6 = linkProperties?.linkAddresses?.firstOrNull { it.address is java.net.Inet6Address }?.address?.hostAddress

        emit(InternetDetails(
            publicIp = "Fetching...", // Usually needs a network call, logic exists in ViewModel/Helper
            localIp = localIp ?: "—",
            ipv6 = ipv6 ?: "—",
            gateway = gateway ?: "—",
            dns1 = dnsServers.getOrNull(0) ?: "—",
            dns2 = dnsServers.getOrNull(1) ?: "—",
            mac = getMacAddress()
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

    fun getMobileNetworkInfo(): Flow<MobileNetworkInfo> = flow {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        
        val simOperator = tm.simOperator ?: "—"
        val networkOperator = tm.networkOperator ?: "—"
        val carrierName = tm.networkOperatorName ?: "—"
        val simCountry = tm.simCountryIso?.uppercase() ?: "—"
        
        val mcc = if (networkOperator.length >= 3) networkOperator.substring(0, 3) else "—"
        val mnc = if (networkOperator.length >= 5) networkOperator.substring(3) else "—"
        
        val roamingStatus = if (tm.isNetworkRoaming) "Roaming" else "Not Roaming"
        
        val networkType = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            try {
                when (tm.dataNetworkType) {
                    TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE,
                    TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT,
                    TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
                    TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0,
                    TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA,
                    TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA,
                    TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD,
                    TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
                    TelephonyManager.NETWORK_TYPE_LTE -> "4G (LTE)"
                    TelephonyManager.NETWORK_TYPE_NR -> "5G"
                    else -> "Unknown"
                }
            } catch (e: SecurityException) { "—" }
        } else "—"

        var signalStr = "—"
        var lteSignal = "—"
        var nrSignal = "—"
        var cellId = "—"
        var tac = "—"
        var pci = "—"
        
        try {
            val allCellInfo = tm.allCellInfo
            if (!allCellInfo.isNullOrEmpty()) {
                val primaryCell = allCellInfo.firstOrNull { it.isRegistered } ?: allCellInfo.first()
                
                when (primaryCell) {
                    is CellInfoLte -> {
                        val identity = primaryCell.cellIdentity
                        cellId = identity.ci.toString()
                        tac = identity.tac.toString()
                        pci = identity.pci.toString()
                        lteSignal = "${primaryCell.cellSignalStrength.dbm} dBm"
                        signalStr = lteSignal
                    }
                    is CellInfoGsm -> {
                        cellId = primaryCell.cellIdentity.cid.toString()
                        signalStr = "${primaryCell.cellSignalStrength.dbm} dBm"
                    }
                    is CellInfoWcdma -> {
                        cellId = primaryCell.cellIdentity.cid.toString()
                        signalStr = "${primaryCell.cellSignalStrength.dbm} dBm"
                    }
                    is CellInfoCdma -> {
                        cellId = primaryCell.cellIdentity.basestationId.toString()
                        signalStr = "${primaryCell.cellSignalStrength.dbm} dBm"
                    }
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    allCellInfo.filterIsInstance<CellInfoNr>().firstOrNull()?.let { nrCell ->
                        nrSignal = "${nrCell.cellSignalStrength.dbm} dBm"
                        if (primaryCell is CellInfoNr) signalStr = nrSignal
                    }
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted
        }

        emit(MobileNetworkInfo(
            simOperator = simOperator,
            networkOperator = networkOperator,
            carrierName = carrierName,
            simCountry = simCountry,
            mcc = mcc,
            mnc = mnc,
            roamingStatus = roamingStatus,
            networkGeneration = networkType,
            signalStrength = signalStr,
            lteSignalStrength = lteSignal,
            nrSignalStrength = nrSignal,
            cellId = cellId,
            tac = tac,
            pci = pci,
            registeredNetwork = if (tm.isNetworkRoaming) "Roaming" else "Home Network",
            preferredNetworkType = "Auto"
        ))
    }

    fun getDeviceInfo(): Flow<DeviceInfo> = flow {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val displayMetrics = context.resources.displayMetrics
        val defaultDisplay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                context.display
            } catch (e: Exception) {
                val dm = context.getSystemService(Context.DISPLAY_SERVICE) as android.hardware.display.DisplayManager
                dm.getDisplay(android.view.Display.DEFAULT_DISPLAY)
            }
        } else {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay
        }
        
        val stat = android.os.StatFs(android.os.Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        val totalStorageBytes = totalBlocks * blockSize
        val availableStorageBytes = availableBlocks * blockSize
        val usedStorageBytes = totalStorageBytes - availableStorageBytes

        val batteryStatus: Intent? = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (scale > 0) (level * 100 / scale.toFloat()).toInt() else -1
        val status = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                status == android.os.BatteryManager.BATTERY_STATUS_FULL
        val chargePlug = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val chargingType = when (chargePlug) {
            android.os.BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            android.os.BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            android.os.BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "—"
        }

        val health = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val healthLabel = when (health) {
            android.os.BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            android.os.BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            android.os.BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            android.os.BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            android.os.BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            else -> "Unknown"
        }
        val temp = (batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
        val voltage = (batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_VOLTAGE, 0) ?: 0) / 1000f
        val technology = batteryStatus?.getStringExtra(android.os.BatteryManager.EXTRA_TECHNOLOGY) ?: "—"

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager

        emit(DeviceInfo(
            brand = Build.BRAND,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            device = Build.DEVICE,
            product = Build.PRODUCT,
            board = Build.BOARD,
            hardware = Build.HARDWARE,
            androidVersion = "Android ${Build.VERSION.RELEASE}",
            sdk = Build.VERSION.SDK_INT,
            securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "—",
            bootloader = Build.BOOTLOADER,
            kernelVersion = System.getProperty("os.version") ?: "—",
            buildNumber = Build.DISPLAY,
            supportedAbis = Build.SUPPORTED_ABIS.joinToString(", "),
            
            totalRam = formatSize(memoryInfo.totalMem),
            availableRam = formatSize(memoryInfo.availMem),
            usedRam = formatSize(memoryInfo.totalMem - memoryInfo.availMem),
            isLowMemory = memoryInfo.lowMemory,
            
            totalStorage = formatSize(totalStorageBytes),
            availableStorage = formatSize(availableStorageBytes),
            usedStorage = formatSize(usedStorageBytes),
            storageUsagePercent = ((usedStorageBytes.toFloat() / totalStorageBytes.toFloat()) * 100).toInt(),
            
            resolution = "${displayMetrics.widthPixels} x ${displayMetrics.heightPixels}",
            density = displayMetrics.densityDpi,
            refreshRate = "${defaultDisplay?.refreshRate?.toInt() ?: 0} Hz",
            screenSize = calculateScreenSize(displayMetrics),
            orientation = if (context.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) "Landscape" else "Portrait",

            batteryLevel = batteryPct,
            isCharging = isCharging,
            chargingType = chargingType,
            batteryHealth = healthLabel,
            batteryTemp = "$temp°C",
            batteryVoltage = "$voltage V",
            batteryTech = technology,
            isPowerSaveMode = powerManager.isPowerSaveMode
        ))
    }

    private fun formatSize(bytes: Long): String {
        return Formatter.formatFileSize(context, bytes)
    }

    private fun calculateScreenSize(metrics: android.util.DisplayMetrics): String {
        val x = Math.pow(metrics.widthPixels.toDouble() / metrics.xdpi.toDouble(), 2.0)
        val y = Math.pow(metrics.heightPixels.toDouble() / metrics.ydpi.toDouble(), 2.0)
        val screenInches = Math.sqrt(x + y)
        return "%.1f\"".format(screenInches)
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

    private fun getMacAddress(): String {
        return try {
            val all = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.name.equals("wlan0", ignoreCase = true)) continue
                val macBytes = nif.hardwareAddress ?: return "—"
                val res1 = StringBuilder()
                for (b in macBytes) {
                    res1.append(String.format("%02X:", b))
                }
                if (res1.isNotEmpty()) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
            "—"
        } catch (ex: Exception) {
            "—"
        }
    }
}

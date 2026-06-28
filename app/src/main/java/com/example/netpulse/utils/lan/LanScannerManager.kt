package com.example.netpulse.utils.lan

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.text.format.Formatter
import com.example.netpulse.data.lan.DeviceType
import com.example.netpulse.data.lan.LanDevice
import com.example.netpulse.data.lan.NetworkInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.concurrent.Executors

class LanScannerManager(private val context: Context) {
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _discoveredDevices = MutableStateFlow<List<LanDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<LanDevice>> = _discoveredDevices

    private var scanJob: Job? = null

    fun getNetworkInfo(): NetworkInfo {
        val info = wifiManager.connectionInfo
        val localIp = Formatter.formatIpAddress(info.ipAddress)
        val gatewayIp = Formatter.formatIpAddress(wifiManager.dhcpInfo.gateway)
        val ssid = info.ssid?.removeSurrounding("\"") ?: "Unknown"
        
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val lp = cm.getLinkProperties(cm.activeNetwork)
        val dns1 = lp?.dnsServers?.getOrNull(0)?.hostAddress ?: "—"
        val dns2 = lp?.dnsServers?.getOrNull(1)?.hostAddress ?: "—"

        return NetworkInfo(
            gatewayIp = gatewayIp,
            localIp = localIp,
            ssid = if (ssid == "<unknown ssid>") "Mobile Hotspot/Hidden" else ssid,
            dns1 = dns1,
            dns2 = dns2
        )
    }

    fun startScan() {
        scanJob?.cancel()
        _isScanning.value = true
        _discoveredDevices.value = emptyList()

        val networkInfo = getNetworkInfo()
        if (networkInfo.localIp == "0.0.0.0") {
            _isScanning.value = false
            return
        }
        
        val baseIp = networkInfo.localIp.substringBeforeLast(".") + "."

        scanJob = CoroutineScope(Dispatchers.IO).launch {
            val devices = mutableListOf<LanDevice>()
            
            // 1. Add known local entities
            val currentDevice = LanDevice(
                ipAddress = networkInfo.localIp,
                isCurrentDevice = true,
                hostname = android.os.Build.MODEL,
                deviceType = DeviceType.PHONE,
                latencyMs = 0,
                vendor = android.os.Build.MANUFACTURER,
                lastSeen = System.currentTimeMillis()
            )
            devices.add(currentDevice)
            
            if (networkInfo.gatewayIp != "0.0.0.0" && networkInfo.gatewayIp != networkInfo.localIp) {
                val gatewayLatency = measureLatency(networkInfo.gatewayIp)
                devices.add(LanDevice(
                    ipAddress = networkInfo.gatewayIp,
                    isRouter = true,
                    hostname = "Gateway / Router",
                    deviceType = DeviceType.ROUTER,
                    latencyMs = gatewayLatency,
                    lastSeen = System.currentTimeMillis()
                ))
            }
            _discoveredDevices.value = devices.toList()

            // 2. Parallel Ping & Port Scan Sweep (Optimized thread pool)
            val dispatcher = Executors.newFixedThreadPool(40).asCoroutineDispatcher()
            val jobs = (1..254).map { i ->
                async(dispatcher) {
                    val ip = baseIp + i
                    if (ip == networkInfo.localIp || ip == networkInfo.gatewayIp) return@async
                    
                    // High-performance reachability check
                    val reachable = isDeviceReachable(ip)
                    if (reachable) {
                        val hostname = resolveHostname(ip)
                        val type = classifyDevice(hostname, ip)
                        val latency = measureLatency(ip)
                        
                        val device = LanDevice(
                            ipAddress = ip,
                            hostname = hostname,
                            isOnline = true,
                            deviceType = type,
                            latencyMs = latency,
                            lastSeen = System.currentTimeMillis()
                        )
                        
                        synchronized(devices) {
                            devices.removeAll { it.ipAddress == ip }
                            devices.add(device)
                            _discoveredDevices.value = devices.toList().sortedWith(
                                compareByDescending<LanDevice> { it.isCurrentDevice }
                                .thenByDescending { it.isRouter }
                                .thenBy { it.ipAddress.split(".").map { p -> p.padStart(3, '0') }.joinToString(".") }
                            )
                        }
                    }
                }
            }
            jobs.awaitAll()
            dispatcher.close()
            _isScanning.value = false
        }
    }

    private fun isDeviceReachable(ip: String): Boolean {
        return try {
            val address = InetAddress.getByName(ip)
            // Try isReachable first (ICMP)
            if (address.isReachable(500)) return true
            
            // Fallback: Try common ports
            val ports = intArrayOf(80, 443, 445, 135, 139, 8080)
            for (port in ports) {
                if (isPortOpen(ip, port, 200)) return true
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun isPortOpen(ip: String, port: Int, timeout: Int): Boolean {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ip, port), timeout)
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun resolveHostname(ip: String): String {
        return try {
            val address = InetAddress.getByName(ip)
            val host = address.canonicalHostName
            if (host == ip) "Unknown" else host
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun measureLatency(ip: String): Long {
        val start = System.currentTimeMillis()
        return if (isDeviceReachable(ip)) {
            System.currentTimeMillis() - start
        } else {
            -1
        }
    }

    private fun classifyDevice(hostname: String, ip: String): DeviceType {
        val h = hostname.lowercase()
        return when {
            h.contains("router") || h.contains("gateway") || ip.endsWith(".1") -> DeviceType.ROUTER
            h.contains("phone") || h.contains("android") || h.contains("iphone") -> DeviceType.PHONE
            h.contains("pad") || h.contains("tablet") || h.contains("ipad") -> DeviceType.TABLET
            h.contains("laptop") || h.contains("macbook") -> DeviceType.LAPTOP
            h.contains("desktop") || h.contains("pc") || h.contains("computer") -> DeviceType.DESKTOP
            h.contains("print") -> DeviceType.PRINTER
            h.contains("tv") || h.contains("television") || h.contains("bravia") || h.contains("viera") -> DeviceType.TV
            h.contains("nas") || h.contains("storage") || h.contains("synology") || h.contains("qnap") -> DeviceType.NAS
            h.contains("cam") || h.contains("camera") -> DeviceType.CAMERA
            h.contains("xbox") || h.contains("playstation") || h.contains("ps4") || h.contains("ps5") || h.contains("nintendo") -> DeviceType.CONSOLE
            else -> DeviceType.UNKNOWN
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        _isScanning.value = false
    }
}

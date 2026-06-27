package com.example.netpulse.utils.lan

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.text.format.Formatter
import com.example.netpulse.data.lan.LanDevice
import com.example.netpulse.data.lan.NetworkInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.InetAddress
import java.net.NetworkInterface

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
        
        return NetworkInfo(
            gatewayIp = gatewayIp,
            localIp = localIp,
            ssid = ssid
        )
    }

    fun startScan() {
        scanJob?.cancel()
        _isScanning.value = true
        _discoveredDevices.value = emptyList()

        val networkInfo = getNetworkInfo()
        val baseIp = networkInfo.localIp.substringBeforeLast(".") + "."

        scanJob = CoroutineScope(Dispatchers.IO).launch {
            val devices = mutableListOf<LanDevice>()
            
            // Add current device and router manually for quick feedback if valid
            if (networkInfo.localIp != "0.0.0.0") {
                devices.add(LanDevice(ipAddress = networkInfo.localIp, isCurrentDevice = true, hostname = "My Phone"))
            }
            if (networkInfo.gatewayIp != "0.0.0.0" && networkInfo.gatewayIp != networkInfo.localIp) {
                devices.add(LanDevice(ipAddress = networkInfo.gatewayIp, isRouter = true, hostname = "Router"))
            }
            _discoveredDevices.value = devices.toList()

            val jobs = (1..254).map { i ->
                async {
                    val ip = baseIp + i
                    if (ip == networkInfo.localIp || ip == networkInfo.gatewayIp) return@async
                    
                    try {
                        val address = InetAddress.getByName(ip)
                        if (address.isReachable(1000)) {
                            val hostname = try { address.canonicalHostName } catch (e: Exception) { "Unknown" }
                            val device = LanDevice(
                                ipAddress = ip,
                                hostname = hostname,
                                isOnline = true
                            )
                            synchronized(devices) {
                                devices.add(device)
                                _discoveredDevices.value = devices.toList().sortedBy { it.ipAddress }
                            }
                        }
                    } catch (e: Exception) {
                        // Skip unreachable
                    }
                }
            }
            jobs.awaitAll()
            _isScanning.value = false
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        _isScanning.value = false
    }
}

package com.example.netpulse.utils.wifi

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import com.example.netpulse.data.wifi.WifiNetwork
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WifiScannerManager(private val context: Context) {

    private val wifiManager = context
        .applicationContext
        .getSystemService(Context.WIFI_SERVICE) 
        as WifiManager

    private val _scanResults = 
        MutableStateFlow<List<WifiNetwork>>(emptyList())
    val scanResults: StateFlow<List<WifiNetwork>> = 
        _scanResults

    private val _scanState = 
        MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState

    private var scanReceiver: BroadcastReceiver? = null

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (_scanState.value is ScanState.Scanning) return

        _scanState.value = ScanState.Scanning
        _scanResults.value = emptyList()

        // Register receiver BEFORE starting scan
        scanReceiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context, 
                intent: Intent
            ) {
                if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                    processScanResults()
                }
            }
        }

        val intentFilter = IntentFilter(
            WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
        )
        context.registerReceiver(scanReceiver, intentFilter)

        // Start scan
        val scanStarted = try {
            wifiManager.startScan()
        } catch (e: Exception) {
            false
        }

        if (!scanStarted) {
            // On Android 9+ startScan is throttled
            // Fall back to cached results
            processScanResults()
        }
    }

    fun stopScan() {
        unregisterReceiver()
        _scanState.value = ScanState.Idle
    }

    @SuppressLint("MissingPermission")
    private fun processScanResults() {
        unregisterReceiver()

        try {
            val results = wifiManager.scanResults
            val networks = results.map { result ->
                WifiNetwork(
                    ssid = result.SSID?.removeSurrounding("\"") ?: "",
                    bssid = result.BSSID ?: "",
                    signalStrength = result.level,
                    frequency = result.frequency,
                    capabilities = result.capabilities ?: "",
                    channelWidth = getChannelWidth(result),
                    standard = getWifiStandard(result),
                    channel = frequencyToChannel(
                        result.frequency
                    ),
                    timestamp = System.currentTimeMillis()
                )
            }.sortedByDescending { it.signalStrength }

            _scanResults.value = networks
            _scanState.value = ScanState.Complete(
                count = networks.size
            )
            
            // Log for debugging
            println("WifiScanner: Found ${networks.size} networks")
        } catch (e: SecurityException) {
            _scanState.value = ScanState.Error(
                "Location permission required for WiFi scan"
            )
        } catch (e: Exception) {
            _scanState.value = ScanState.Error(
                e.message ?: "Unknown scan error"
            )
        }
    }

    private fun unregisterReceiver() {
        scanReceiver?.let {
            try { context.unregisterReceiver(it) } 
            catch (e: Exception) { }
            scanReceiver = null
        }
    }

    private fun getChannelWidth(
        result: ScanResult
    ): String {
        return if (Build.VERSION.SDK_INT >= 
            Build.VERSION_CODES.M) {
            when (result.channelWidth) {
                ScanResult.CHANNEL_WIDTH_20MHZ -> "20 MHz"
                ScanResult.CHANNEL_WIDTH_40MHZ -> "40 MHz"
                ScanResult.CHANNEL_WIDTH_80MHZ -> "80 MHz"
                ScanResult.CHANNEL_WIDTH_160MHZ -> "160 MHz"
                ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ 
                    -> "80+80 MHz"
                else -> "Unknown"
            }
        } else "Unknown"
    }

    private fun getWifiStandard(
        result: ScanResult
    ): String {
        return if (Build.VERSION.SDK_INT >= 
            Build.VERSION_CODES.R) {
            when (result.wifiStandard) {
                ScanResult.WIFI_STANDARD_LEGACY -> "802.11b/g"
                ScanResult.WIFI_STANDARD_11N -> "802.11n (WiFi 4)"
                ScanResult.WIFI_STANDARD_11AC -> "802.11ac (WiFi 5)"
                ScanResult.WIFI_STANDARD_11AX -> "802.11ax (WiFi 6)"
                ScanResult.WIFI_STANDARD_11AD -> "802.11ad"
                else -> "Unknown"
            }
        } else {
            when {
                result.capabilities.contains("WPA3") -> 
                    "802.11ax (WiFi 6)"
                result.capabilities.contains("WPA2") -> 
                    "802.11ac (WiFi 5)"
                else -> "802.11n (WiFi 4)"
            }
        }
    }

    private fun frequencyToChannel(freq: Int): Int {
        return when {
            freq == 2484 -> 14
            freq in 2412..2484 -> (freq - 2412) / 5 + 1
            freq in 5170..5825 -> (freq - 5170) / 5 + 34
            freq in 5955..7115 -> (freq - 5955) / 5 + 1
            else -> 0
        }
    }
}

sealed class ScanState {
    object Idle : ScanState()
    object Scanning : ScanState()
    data class Complete(val count: Int) : ScanState()
    data class Error(val message: String) : ScanState()
}

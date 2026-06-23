package com.example.netpulse.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.telephony.TelephonyManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class NetworkState(
    val isConnected: Boolean = false,
    val networkType: String = "No Connection",
    val signalIcon: NetworkIcon = NetworkIcon.NONE
)

enum class NetworkIcon {
    WIFI, MOBILE_2G, MOBILE_3G, MOBILE_4G, MOBILE_5G,
    ETHERNET, NONE
}

class NetworkStateManager(private val context: Context) {

    private val connectivityManager = context
        .getSystemService(Context.CONNECTIVITY_SERVICE)
        as ConnectivityManager

    private val _networkState = MutableStateFlow(NetworkState())
    val networkState: StateFlow<NetworkState> = _networkState

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            updateNetworkState()
        }

        override fun onLost(network: Network) {
            // Small delay to avoid flicker when switching between WiFi and mobile
            CoroutineScope(Dispatchers.IO).launch {
                delay(500)
                updateNetworkState()
            }
        }

        override fun onCapabilitiesChanged(
            network: Network,
            capabilities: NetworkCapabilities
        ) {
            updateNetworkState(capabilities)
        }

        override fun onUnavailable() {
            _networkState.value = NetworkState(
                isConnected = false,
                networkType = "No Connection",
                signalIcon = NetworkIcon.NONE
            )
        }
    }

    fun startObserving() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
            // Get current state immediately on start
            updateNetworkState()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopObserving() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Already unregistered — ignore
        }
    }

    private fun updateNetworkState(caps: NetworkCapabilities? = null) {
        val network = connectivityManager.activeNetwork
        val capabilities = caps ?: connectivityManager.getNetworkCapabilities(network)

        if (capabilities == null || network == null) {
            _networkState.value = NetworkState(
                isConnected = false,
                networkType = "No Connection",
                signalIcon = NetworkIcon.NONE
            )
            return
        }

        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        if (!hasInternet) {
            _networkState.value = NetworkState(
                isConnected = false,
                networkType = "No Connection",
                signalIcon = NetworkIcon.NONE
            )
            return
        }

        when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                _networkState.value = NetworkState(
                    isConnected = true,
                    networkType = "WiFi",
                    signalIcon = NetworkIcon.WIFI
                )
            }

            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                val cellType = detectCellularType(capabilities)
                _networkState.value = NetworkState(
                    isConnected = true,
                    networkType = cellType.first,
                    signalIcon = cellType.second
                )
            }

            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                _networkState.value = NetworkState(
                    isConnected = true,
                    networkType = "Ethernet",
                    signalIcon = NetworkIcon.ETHERNET
                )
            }

            else -> {
                _networkState.value = NetworkState(
                    isConnected = true,
                    networkType = "Connected",
                    signalIcon = NetworkIcon.NONE
                )
            }
        }
    }

    private fun detectCellularType(capabilities: NetworkCapabilities): Pair<String, NetworkIcon> {
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            val networkType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tm.dataNetworkType
            } else {
                @Suppress("DEPRECATION")
                tm.networkType
            }

            return when (networkType) {
                TelephonyManager.NETWORK_TYPE_NR -> Pair("5G", NetworkIcon.MOBILE_5G)
                TelephonyManager.NETWORK_TYPE_LTE -> Pair("4G", NetworkIcon.MOBILE_4G)
                TelephonyManager.NETWORK_TYPE_HSPAP,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_EVDO_B -> Pair("3G", NetworkIcon.MOBILE_3G)
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_1xRTT,
                TelephonyManager.NETWORK_TYPE_IDEN -> Pair("2G", NetworkIcon.MOBILE_2G)
                else -> fallbackCellularDetection(capabilities)
            }
        } catch (e: SecurityException) {
            return fallbackCellularDetection(capabilities)
        } catch (e: Exception) {
            return fallbackCellularDetection(capabilities)
        }
    }

    private fun fallbackCellularDetection(capabilities: NetworkCapabilities): Pair<String, NetworkIcon> {
        val downKbps = capabilities.linkDownstreamBandwidthKbps
        return when {
            downKbps >= 50_000 -> Pair("5G", NetworkIcon.MOBILE_5G)
            downKbps >= 10_000 -> Pair("4G", NetworkIcon.MOBILE_4G)
            downKbps >= 1_000 -> Pair("3G", NetworkIcon.MOBILE_3G)
            else -> Pair("2G", NetworkIcon.MOBILE_2G)
        }
    }
}

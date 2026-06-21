package com.example.netpulse.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

class WiFiAutoRunManager(
    private val context: Context,
    private val onWiFiConnected: () -> Unit
) {
    private val connectivityManager = context
        .getSystemService(Context.CONNECTIVITY_SERVICE)
        as ConnectivityManager

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun startWatching() {
        if (networkCallback != null) return

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // WiFi just connected with internet — trigger test
                onWiFiConnected()
            }
        }

        connectivityManager.registerNetworkCallback(request, networkCallback!!)
    }

    fun stopWatching() {
        networkCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
            networkCallback = null
        }
    }
}

package com.example.netpulse.data.network

import android.util.Log

// Debug info to show during testing:
data class DebugInfo(
    val phase: String,
    val totalBytes: Long,
    val measurementDuration: Long,
    val sampleCount: Int,
    val rawSamples: List<Double>,
    val finalResult: Double,
    val serverUsed: String = "Cloudflare",
    val connectionCount: Int = 3
)

object SpeedTestValidator {
    private const val TAG = "SpeedTest"

    fun logDebugInfo(info: DebugInfo) {
        Log.d(TAG, "--------------------------------------------------")
        Log.d(TAG, "PHASE: ${info.phase}")
        Log.d(TAG, "Total Bytes: ${info.totalBytes}")
        Log.d(TAG, "Duration: ${info.measurementDuration}ms")
        Log.d(TAG, "Samples: ${info.sampleCount}")
        Log.d(TAG, "Final Result: ${info.finalResult} Mbps")
        Log.d(TAG, "Server: ${info.serverUsed}")
        Log.d(TAG, "Connections: ${info.connectionCount}")
        // Log.d(TAG, "Raw Samples: ${info.rawSamples}")
        Log.d(TAG, "--------------------------------------------------")
    }
}

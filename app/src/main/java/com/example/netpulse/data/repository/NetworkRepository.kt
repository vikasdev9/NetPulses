package com.example.netpulse.data.repository

import com.example.netpulse.ui.viewmodel.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NetworkRepository {
    
    fun getNetworkStatus(): Flow<NetworkStatus> = flow {
        // Mocking real-time network data
        emit(NetworkStatus())
    }

    fun getInternetDetails(): Flow<InternetDetails> = flow {
        emit(InternetDetails())
    }

    fun getIspInfo(): Flow<IspInfo> = flow {
        emit(IspInfo())
    }

    fun getSpeedSummary(): Flow<SpeedSummary> = flow {
        emit(SpeedSummary())
    }

    fun getDataUsage(): Flow<DataUsage> = flow {
        emit(DataUsage())
    }

    fun getDeviceInfo(): Flow<DeviceInfo> = flow {
        emit(DeviceInfo())
    }

    fun getTimeline(): Flow<List<TimelineEvent>> = flow {
        emit(listOf(
            TimelineEvent("14:20", "Test Finished", "Download: 452 Mbps, Upload: 180 Mbps", TimelineType.TEST_FINISHED),
            TimelineEvent("14:18", "Test Started", "Manual speed test execution", TimelineType.TEST_STARTED),
            TimelineEvent("12:05", "IP Changed", "Public IP updated by ISP", TimelineType.IP_CHANGED),
            TimelineEvent("09:00", "Connected", "WiFi connection established", TimelineType.CONNECTED)
        ))
    }

    fun getDiagnostics(): Flow<AdvancedDiagnostics> = flow {
        emit(AdvancedDiagnostics())
    }

    fun getSecurityStatus(): Flow<SecurityStatus> = flow {
        emit(SecurityStatus())
    }

    fun getRecommendations(): Flow<List<String>> = flow {
        emit(listOf(
            "Move closer to router for better signal.",
            "Switch to 5GHz for lower latency.",
            "Network performing normally.",
            "Disable VPN for better speed."
        ))
    }
}

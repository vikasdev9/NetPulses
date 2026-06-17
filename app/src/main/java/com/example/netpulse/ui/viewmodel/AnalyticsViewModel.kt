package com.example.netpulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netpulse.data.repository.NetworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val repository: NetworkRepository = NetworkRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            combine(
                repository.getNetworkStatus(),
                repository.getInternetDetails(),
                repository.getIspInfo(),
                repository.getSpeedSummary(),
                repository.getDataUsage(),
                repository.getDeviceInfo(),
                repository.getTimeline(),
                repository.getDiagnostics(),
                repository.getSecurityStatus(),
                repository.getRecommendations()
            ) { results ->
                _uiState.value = _uiState.value.copy(
                    networkStatus = results[0] as NetworkStatus,
                    internetDetails = results[1] as InternetDetails,
                    ispInfo = results[2] as IspInfo,
                    speedSummary = results[3] as SpeedSummary,
                    dataUsage = results[4] as DataUsage,
                    deviceInfo = results[5] as DeviceInfo,
                    timeline = results[6] as List<TimelineEvent>,
                    diagnostics = results[7] as AdvancedDiagnostics,
                    security = results[8] as SecurityStatus,
                    recommendations = results[9] as List<String>,
                    isLoading = false
                )
            }.collect()
        }
    }

    fun refresh() {
        loadData()
    }
}

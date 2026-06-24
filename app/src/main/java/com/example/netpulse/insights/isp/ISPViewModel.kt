package com.example.netpulse.insights.isp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netpulse.data.datastore.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ISPState(
    val ispName: String = "Unknown ISP",
    val actualSpeed: Double = 0.0,
    val advertisedSpeed: Int = 100,
    val deliveryScore: Int = 0,
    val reliabilityScore: Int = 0,
    val rankLabel: String = "Average",
    val isLoading: Boolean = true
)

class ISPViewModel(
    private val ispDao: ISPDao,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ISPState())
    val uiState: StateFlow<ISPState> = _uiState.asStateFlow()

    init {
        loadISPMetrics()
    }

    private fun loadISPMetrics() {
        viewModelScope.launch {
            combine(
                ispDao.getAll(),
                userPreferences.dataPlanLimitGb // Reusing this for advertised speed as a placeholder or could add a new key
            ) { isps, advertised ->
                if (isps.isEmpty()) return@combine ISPState(isLoading = false)

                val isp = isps.first() // Show most recent or primary ISP
                val advertisedVal = advertised.coerceAtLeast(1)
                val delivery = (isp.avgDownload / advertisedVal * 100).toInt().coerceAtMost(100)
                val reliability = if (isp.testCount > 0) (isp.testsAboveHalfAdvertised.toDouble() / isp.testCount * 100).toInt() else 0
                
                val rank = when {
                    delivery > 90 -> "Top 10%"
                    delivery < 60 -> "Below Average"
                    else -> "Average"
                }

                ISPState(
                    ispName = isp.name,
                    actualSpeed = isp.avgDownload,
                    advertisedSpeed = advertisedVal,
                    deliveryScore = delivery,
                    reliabilityScore = reliability,
                    rankLabel = rank,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

package com.example.netpulse.thermal.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.netpulse.data.datastore.UserPreferences
import com.example.netpulse.thermal.model.*
import com.example.netpulse.thermal.repository.ThermalRepository
import com.example.netpulse.thermal.worker.ThermalWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ThermalViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)
    private val repository = ThermalRepository(application, userPreferences)

    private val _currentThermalData = MutableStateFlow(repository.getLatestThermalData())
    val currentThermalData: StateFlow<ThermalData> = _currentThermalData.asStateFlow()

    private val _history = MutableStateFlow<List<com.example.netpulse.thermal.database.ThermalEntity>>(emptyList())
    val history: StateFlow<List<com.example.netpulse.thermal.database.ThermalEntity>> = _history.asStateFlow()

    val settings = combine(
        userPreferences.thermalMonitorEnabled,
        userPreferences.thermalMonitorInterval,
        userPreferences.thermalHighThreshold,
        userPreferences.thermalCriticalThreshold,
        userPreferences.thermalBackgroundMonitoring,
        userPreferences.thermalSaveHistory
    ) { values ->
        ThermalSettingsState(
            enabled = values[0] as Boolean,
            interval = values[1] as String,
            highThreshold = values[2] as Float,
            criticalThreshold = values[3] as Float,
            backgroundMonitoring = values[4] as Boolean,
            saveHistory = values[5] as Boolean
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThermalSettingsState())

    init {
        startLiveUpdates()
        loadHistory()
    }

    private fun startLiveUpdates() {
        viewModelScope.launch {
            while (true) {
                _currentThermalData.value = repository.getLatestThermalData()
                delay(2000) // 2 seconds update while screen is on
            }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            repository.getHistory().collect {
                _history.value = it
            }
        }
    }

    fun toggleMonitoring(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setThermalMonitorEnabled(enabled)
            if (enabled) {
                val interval = parseInterval(userPreferences.thermalMonitorInterval.first())
                ThermalWorker.schedule(getApplication(), interval)
            } else {
                ThermalWorker.cancel(getApplication())
            }
        }
    }

    fun setMonitoringInterval(interval: String) {
        viewModelScope.launch {
            userPreferences.setThermalMonitorInterval(interval)
            if (userPreferences.thermalMonitorEnabled.first()) {
                ThermalWorker.schedule(getApplication(), parseInterval(interval))
            }
        }
    }

    private fun parseInterval(interval: String): Long {
        return when (interval) {
            "30 Seconds" -> 1 // PeriodicWork minimum is 15 minutes, but we can use 1 min for testing if forced
            "1 Minute" -> 1
            "2 Minutes" -> 2
            "5 Minutes" -> 5
            "10 Minutes" -> 10
            "15 Minutes" -> 15
            "30 Minutes" -> 30
            "60 Minutes" -> 60
            else -> 15
        }
    }

    fun refreshData() {
        _currentThermalData.value = repository.getLatestThermalData()
    }

    fun calculateThermalScore(data: ThermalData): Int {
        var score = 100
        
        // Temperature penalty
        if (data.temperature > 40) score -= 20
        if (data.temperature > 45) score -= 30
        if (data.temperature > 50) score -= 40
        
        // Charging penalty
        if (data.isCharging && data.temperature > 42) score -= 10
        
        return score.coerceIn(0, 100)
    }

    fun getScoreCategory(score: Int): ThermalScore {
        return when {
            score >= 90 -> ThermalScore.EXCELLENT
            score >= 75 -> ThermalScore.GOOD
            score >= 60 -> ThermalScore.AVERAGE
            score >= 40 -> ThermalScore.POOR
            else -> ThermalScore.CRITICAL
        }
    }
}

data class ThermalSettingsState(
    val enabled: Boolean = false,
    val interval: String = "5 Minutes",
    val highThreshold: Float = 42f,
    val criticalThreshold: Float = 47f,
    val backgroundMonitoring: Boolean = true,
    val saveHistory: Boolean = true
)

package com.example.netpulse.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.netpulse.data.datastore.UserPreferences
import com.example.netpulse.worker.SpeedTestWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SpeedTestSettingsViewModel(
    private val application: Application,
    private val userPreferences: UserPreferences
) : AndroidViewModel(application) {

    val speedDropAlertEnabled: StateFlow<Boolean> = userPreferences.speedDropAlertEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val autoScheduleEnabled: StateFlow<Boolean> = userPreferences.autoScheduleEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val baselineSpeed: StateFlow<Float> = userPreferences.baselineSpeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val lastAutoTestTime: StateFlow<Long> = userPreferences.lastAutoTestTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun toggleSpeedDropAlert(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setSpeedDropAlertEnabled(enabled)
        }
    }

    fun toggleAutoSchedule(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setAutoScheduleEnabled(enabled)
            if (enabled) {
                scheduleWork()
            } else {
                cancelWork()
            }
        }
    }

    fun updateBaseline(speed: Float) {
        viewModelScope.launch {
            userPreferences.setBaselineSpeed(speed)
        }
    }

    private fun scheduleWork() {
        val workRequest = PeriodicWorkRequest.Builder(SpeedTestWorker::class.java, 24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag("auto_speed_test")
            .build()

        WorkManager.getInstance(application).enqueueUniquePeriodicWork(
            "auto_speed_test",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun cancelWork() {
        WorkManager.getInstance(application).cancelAllWorkByTag("auto_speed_test")
    }

    class Factory(
        private val application: Application,
        private val userPreferences: UserPreferences
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SpeedTestSettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SpeedTestSettingsViewModel(application, userPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

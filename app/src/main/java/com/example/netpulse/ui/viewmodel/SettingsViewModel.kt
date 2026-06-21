package com.example.netpulse.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.netpulse.data.datastore.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val userPreferences: UserPreferences
) : AndroidViewModel(application) {

    data class SettingsState(
        val parallelConnections: Int = 3,
        val testDurationSeconds: Int = 20,
        val autoRunOnWifi: Boolean = false,
        val isPro: Boolean = false,
        val isDarkMode: Boolean = true,
        val notificationsEnabled: Boolean = false,
        val currentLanguage: String = "English",
        val defaultServer: String = "Automatic",
        val appVersion: String = "1.0.0"
    )

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    init {
        viewModelScope.launch {
            combine(
                userPreferences.parallelConnections,
                userPreferences.testDurationSeconds,
                userPreferences.autoRunOnWifi,
                userPreferences.isPro,
                userPreferences.darkMode,
                userPreferences.notificationsEnabled
            ) { values ->
                SettingsState(
                    parallelConnections = values[0] as Int,
                    testDurationSeconds = values[1] as Int,
                    autoRunOnWifi = values[2] as Boolean,
                    isPro = values[3] as Boolean,
                    isDarkMode = values[4] as Boolean,
                    notificationsEnabled = values[5] as Boolean
                )
            }.collect { 
                _state.value = it 
            }
        }
    }

    fun setParallelConnections(value: Int) {
        viewModelScope.launch {
            userPreferences.setParallelConnections(value)
        }
    }

    fun setTestDuration(seconds: Int) {
        viewModelScope.launch {
            userPreferences.setTestDuration(seconds)
        }
    }

    fun setAutoRunOnWifi(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setAutoRunOnWifi(enabled)
        }
    }

    fun toggleAutoRunOnWifi(enabled: Boolean) {
        setAutoRunOnWifi(enabled)
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDarkMode(enabled)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        setDarkMode(enabled)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotificationsEnabled(enabled)
            if (enabled) {
                com.example.netpulse.service.DailySummaryWorker.schedule(getApplication())
            } else {
                com.example.netpulse.service.DailySummaryWorker.cancel(getApplication())
                com.example.netpulse.utils.NotificationHelper.cancelAll(getApplication())
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        setNotificationsEnabled(enabled)
    }

    fun onUpgradeTapped() {
        viewModelScope.launch {
            userPreferences.setIsPro(true)
        }
    }

    fun onRestorePurchase() {
        // Implementation
    }

    fun onRateApp(context: android.content.Context) {
        com.example.netpulse.utils.SettingsUtils.openPlayStore(context)
    }

    fun onShareApp(context: android.content.Context) {
        com.example.netpulse.utils.SettingsUtils.shareApp(context)
    }

    fun onPrivacyPolicy(context: android.content.Context) {
        com.example.netpulse.utils.SettingsUtils.openPrivacyPolicy(context)
    }
}

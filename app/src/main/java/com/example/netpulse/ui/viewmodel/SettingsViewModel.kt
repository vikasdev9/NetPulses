package com.example.netpulse.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.netpulse.utils.SettingsUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class SettingsState(
    val isDarkMode: Boolean = true,
    val notificationsEnabled: Boolean = false,
    val defaultServer: String = "Auto (Mumbai)",
    val parallelConnections: Int = 4,      // options: 1, 2, 4
    val testDurationSeconds: Int = 20,     // options: 10, 20, 30
    val autoRunOnWifi: Boolean = true,
    val isPro: Boolean = false,
    val appVersion: String = "1.0.0"
)

class SettingsViewModel : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    fun toggleDarkMode(enabled: Boolean) {
        _state.update { it.copy(isDarkMode = enabled) }
    }

    fun toggleNotifications(enabled: Boolean) {
        _state.update { it.copy(notificationsEnabled = enabled) }
    }

    fun setParallelConnections(value: Int) {
        _state.update { it.copy(parallelConnections = value) }
    }

    fun setTestDuration(seconds: Int) {
        _state.update { it.copy(testDurationSeconds = seconds) }
    }

    fun toggleAutoRunOnWifi(enabled: Boolean) {
        _state.update { it.copy(autoRunOnWifi = enabled) }
    }

    fun onUpgradeTapped() {
        // In a real app, this would trigger billing flow
        _state.update { it.copy(isPro = true) }
    }

    fun onRestorePurchase() {
        // In a real app, this would restore from Play Store
    }

    fun onRateApp(context: Context) {
        SettingsUtils.openPlayStore(context)
    }

    fun onShareApp(context: Context) {
        SettingsUtils.shareApp(context)
    }

    fun onPrivacyPolicy(context: Context) {
        SettingsUtils.openPrivacyPolicy(context)
    }
}

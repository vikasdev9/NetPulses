package com.example.netpulse.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.netpulse.data.datastore.UserPreferences
import com.example.netpulse.utils.LocaleUtils
import com.example.netpulse.widget.WidgetRefreshManager
import kotlinx.coroutines.flow.*
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
        val currentLanguageCode: String = "en",
        val defaultServer: String = "Automatic",
        val appVersion: String = "1.0.0",
        
        // Widget States
        val widgetsEnabled: Boolean = true,
        val widgetRefreshInterval: String = "1 Hour",
        val widgetAutoRefreshNetwork: Boolean = true,
        val widgetTheme: String = "System",
        val widgetTransparency: Float = 0f,
        val widgetAccentColor: String = "Blue",
        val widgetShowNetworkName: Boolean = true,
        val widgetShowPublicIp: Boolean = true,
        val widgetShowIsp: Boolean = true,
        val widgetShowHealthScore: Boolean = true,
        val widgetShowLastUpdated: Boolean = true,
        val widgetOpenDestination: String = "Home"
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
                userPreferences.notificationsEnabled,
                userPreferences.languageCode,
                
                // Widget Settings
                userPreferences.widgetsEnabled,
                userPreferences.widgetRefreshInterval,
                userPreferences.widgetAutoRefreshNetwork,
                userPreferences.widgetTheme,
                userPreferences.widgetTransparency,
                userPreferences.widgetAccentColor,
                userPreferences.widgetShowNetworkName,
                userPreferences.widgetShowPublicIp,
                userPreferences.widgetShowIsp,
                userPreferences.widgetShowHealthScore,
                userPreferences.widgetShowLastUpdated,
                userPreferences.widgetOpenDestination
            ) { values ->
                val langCode = values[6] as String
                SettingsState(
                    parallelConnections = values[0] as Int,
                    testDurationSeconds = values[1] as Int,
                    autoRunOnWifi = values[2] as Boolean,
                    isPro = values[3] as Boolean,
                    isDarkMode = values[4] as Boolean,
                    notificationsEnabled = values[5] as Boolean,
                    currentLanguageCode = langCode,
                    currentLanguage = LocaleUtils.getLanguageName(langCode),
                    
                    widgetsEnabled = values[7] as Boolean,
                    widgetRefreshInterval = values[8] as String,
                    widgetAutoRefreshNetwork = values[9] as Boolean,
                    widgetTheme = values[10] as String,
                    widgetTransparency = values[11] as Float,
                    widgetAccentColor = values[12] as String,
                    widgetShowNetworkName = values[13] as Boolean,
                    widgetShowPublicIp = values[14] as Boolean,
                    widgetShowIsp = values[15] as Boolean,
                    widgetShowHealthScore = values[16] as Boolean,
                    widgetShowLastUpdated = values[17] as Boolean,
                    widgetOpenDestination = values[18] as String
                )
            }.collect { 
                _state.value = it 
            }
        }
    }

    // --- Core Settings ---
    fun setParallelConnections(value: Int) = viewModelScope.launch { userPreferences.setParallelConnections(value) }
    fun setTestDuration(seconds: Int) = viewModelScope.launch { userPreferences.setTestDuration(seconds) }
    fun setAutoRunOnWifi(enabled: Boolean) = viewModelScope.launch { userPreferences.setAutoRunOnWifi(enabled) }
    fun toggleAutoRunOnWifi(enabled: Boolean) = setAutoRunOnWifi(enabled)
    fun setDarkMode(enabled: Boolean) = viewModelScope.launch { userPreferences.setDarkMode(enabled) }
    fun toggleDarkMode(enabled: Boolean) = setDarkMode(enabled)
    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch {
        userPreferences.setNotificationsEnabled(enabled)
        if (enabled) {
            com.example.netpulse.service.DailySummaryWorker.schedule(getApplication())
        } else {
            com.example.netpulse.service.DailySummaryWorker.cancel(getApplication())
            com.example.netpulse.utils.NotificationHelper.cancelAll(getApplication())
        }
    }
    fun toggleNotifications(enabled: Boolean) = setNotificationsEnabled(enabled)
    fun setLanguage(code: String) = viewModelScope.launch {
        LocaleUtils.saveLanguage(getApplication(), code)
        userPreferences.setLanguageCode(code)
    }

    // --- Widget Settings ---
    fun setWidgetsEnabled(enabled: Boolean) = viewModelScope.launch { 
        userPreferences.setWidgetsEnabled(enabled)
        if (enabled) WidgetRefreshManager.refreshAllWidgets(getApplication())
    }
    fun setWidgetRefreshInterval(interval: String) = viewModelScope.launch { 
        userPreferences.setWidgetRefreshInterval(interval)
        WidgetRefreshManager.updateRefreshInterval(getApplication(), interval)
        WidgetRefreshManager.refreshAllWidgets(getApplication())
    }
    fun setWidgetAutoRefreshNetwork(enabled: Boolean) = viewModelScope.launch { 
        userPreferences.setWidgetAutoRefreshNetwork(enabled)
        WidgetRefreshManager.refreshAllWidgets(getApplication())
    }
    fun setWidgetTheme(theme: String) = viewModelScope.launch { 
        userPreferences.setWidgetTheme(theme)
        WidgetRefreshManager.refreshAllWidgets(getApplication())
    }
    fun setWidgetTransparency(value: Float) = viewModelScope.launch { 
        userPreferences.setWidgetTransparency(value)
        WidgetRefreshManager.refreshAllWidgets(getApplication())
    }
    fun setWidgetAccentColor(color: String) = viewModelScope.launch { 
        userPreferences.setWidgetAccentColor(color)
        WidgetRefreshManager.refreshAllWidgets(getApplication())
    }
    fun setWidgetShowNetworkName(show: Boolean) = viewModelScope.launch { 
        userPreferences.setWidgetShowNetworkName(show)
        WidgetRefreshManager.refreshAllWidgets(getApplication())
    }
    fun setWidgetShowPublicIp(show: Boolean) = viewModelScope.launch { 
        userPreferences.setWidgetShowPublicIp(show)
        WidgetRefreshManager.refreshAllWidgets(getApplication())
    }
    fun setWidgetShowIsp(show: Boolean) = viewModelScope.launch { 
        userPreferences.setWidgetShowIsp(show)
        WidgetRefreshManager.refreshAllWidgets(getApplication())
    }
    fun setWidgetShowHealthScore(show: Boolean) = viewModelScope.launch { 
        userPreferences.setWidgetShowHealthScore(show)
        WidgetRefreshManager.refreshAllWidgets(getApplication())
    }
    fun setWidgetShowLastUpdated(show: Boolean) = viewModelScope.launch { 
        userPreferences.setWidgetShowLastUpdated(show)
        WidgetRefreshManager.refreshAllWidgets(getApplication())
    }
    fun setWidgetOpenDestination(destination: String) = viewModelScope.launch { userPreferences.setWidgetOpenDestination(destination) }

    fun onUpgradeTapped() = viewModelScope.launch { userPreferences.setIsPro(true) }
    fun onRestorePurchase() {}
    fun onRateApp(context: android.content.Context) = com.example.netpulse.utils.SettingsUtils.openPlayStore(context)
    fun onShareApp(context: android.content.Context) = com.example.netpulse.utils.SettingsUtils.shareApp(context)
    fun onPrivacyPolicy(context: android.content.Context) = com.example.netpulse.utils.SettingsUtils.openPrivacyPolicy(context)
}

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
        val widgetOpenDestination: String = "Home",

        // LAN Scanner Settings
        val lanScannerEnabled: Boolean = true,
        val lanAutoScanInterval: String = "Manual",
        val lanScanOnLaunch: Boolean = false,
        val lanScanOnWifiConnected: Boolean = false,
        val lanScanOnNetworkChange: Boolean = false,
        val lanNotifyNewDevice: Boolean = false,
        val lanNotifyDeviceLeaves: Boolean = false,
        val lanNotifyUnknownDevice: Boolean = false,
        val lanRememberNicknames: Boolean = true,
        val lanSaveHistory: Boolean = true,
        val lanShowOfflineDevices: Boolean = true,
        val lanScanEntireNetwork: Boolean = true,
        val lanExportFormat: String = "PDF",

        // Thermal Monitor Settings
        val thermalMonitorEnabled: Boolean = false,
        val thermalBackgroundMonitoring: Boolean = true,
        val thermalMonitorWhileCharging: Boolean = true,
        val thermalMonitorDuringSpeedTest: Boolean = true,
        val thermalMonitorDuringGaming: Boolean = false,
        val thermalMonitorDuringStreaming: Boolean = false,
        val thermalMonitorDuringHeavyNetwork: Boolean = false,
        val thermalMonitorInterval: String = "5 Minutes",
        val thermalHighThreshold: Float = 42f,
        val thermalCriticalThreshold: Float = 47f,
        val thermalNotifyHigh: Boolean = true,
        val thermalNotifyCritical: Boolean = true,
        val thermalNotifySound: Boolean = true,
        val thermalNotifyVibration: Boolean = true,
        val thermalNotifyRingtone: String = "",
        val thermalNotifyPriority: String = "Default",
        val thermalSaveHistory: Boolean = true,
        val thermalMaxHistoryDays: Int = 7,
        val thermalAutoDeleteOld: Boolean = true
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
                userPreferences.widgetOpenDestination,
                
                // LAN Scanner
                userPreferences.lanScannerEnabled,
                userPreferences.lanAutoScanInterval,
                userPreferences.lanScanOnLaunch,
                userPreferences.lanScanOnWifiConnected,
                userPreferences.lanScanOnNetworkChange,
                userPreferences.lanNotifyNewDevice,
                userPreferences.lanNotifyDeviceLeaves,
                userPreferences.lanNotifyUnknownDevice,
                userPreferences.lanRememberNicknames,
                userPreferences.lanSaveHistory,
                userPreferences.lanShowOfflineDevices,
                userPreferences.lanScanEntireNetwork,
                userPreferences.lanExportFormat,
                
                // Thermal Monitor
                userPreferences.thermalMonitorEnabled,
                userPreferences.thermalMonitorInterval,
                userPreferences.thermalSaveHistory,
                userPreferences.thermalNotifyHigh,
                userPreferences.thermalBackgroundMonitoring,
                userPreferences.thermalMonitorWhileCharging,
                userPreferences.thermalMonitorDuringSpeedTest,
                userPreferences.thermalHighThreshold,
                userPreferences.thermalCriticalThreshold
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
                    widgetOpenDestination = values[18] as String,
                    
                    lanScannerEnabled = values[19] as Boolean,
                    lanAutoScanInterval = values[20] as String,
                    lanScanOnLaunch = values[21] as Boolean,
                    lanScanOnWifiConnected = values[22] as Boolean,
                    lanScanOnNetworkChange = values[23] as Boolean,
                    lanNotifyNewDevice = values[24] as Boolean,
                    lanNotifyDeviceLeaves = values[25] as Boolean,
                    lanNotifyUnknownDevice = values[26] as Boolean,
                    lanRememberNicknames = values[27] as Boolean,
                    lanSaveHistory = values[28] as Boolean,
                    lanShowOfflineDevices = values[29] as Boolean,
                    lanScanEntireNetwork = values[30] as Boolean,
                    lanExportFormat = values[31] as String,

                    thermalMonitorEnabled = values[32] as Boolean,
                    thermalMonitorInterval = values[33] as String,
                    thermalSaveHistory = values[34] as Boolean,
                    thermalNotifyHigh = values[35] as Boolean,
                    thermalBackgroundMonitoring = values[36] as Boolean,
                    thermalMonitorWhileCharging = values[37] as Boolean,
                    thermalMonitorDuringSpeedTest = values[38] as Boolean,
                    thermalHighThreshold = values[39] as Float,
                    thermalCriticalThreshold = values[40] as Float
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

    // --- LAN Scanner Settings ---
    fun setLanScannerEnabled(enabled: Boolean) = viewModelScope.launch { userPreferences.setLanScannerEnabled(enabled) }
    fun setLanAutoScanInterval(interval: String) = viewModelScope.launch { userPreferences.setLanAutoScanInterval(interval) }
    fun setLanScanOnLaunch(enabled: Boolean) = viewModelScope.launch { userPreferences.setLanScanOnLaunch(enabled) }
    fun setLanScanOnWifiConnected(enabled: Boolean) = viewModelScope.launch { userPreferences.setLanScanOnWifiConnected(enabled) }
    fun setLanScanOnNetworkChange(enabled: Boolean) = viewModelScope.launch { userPreferences.setLanScanOnNetworkChange(enabled) }
    fun setLanNotifyNewDevice(enabled: Boolean) = viewModelScope.launch { userPreferences.setLanNotifyNewDevice(enabled) }
    fun setLanNotifyDeviceLeaves(enabled: Boolean) = viewModelScope.launch { userPreferences.setLanNotifyDeviceLeaves(enabled) }
    fun setLanNotifyUnknownDevice(enabled: Boolean) = viewModelScope.launch { userPreferences.setLanNotifyUnknownDevice(enabled) }
    fun setLanRememberNicknames(enabled: Boolean) = viewModelScope.launch { userPreferences.setLanRememberNicknames(enabled) }
    fun setLanSaveHistory(enabled: Boolean) = viewModelScope.launch { userPreferences.setLanSaveHistory(enabled) }
    fun setLanShowOfflineDevices(enabled: Boolean) = viewModelScope.launch { userPreferences.setLanShowOfflineDevices(enabled) }
    fun setLanScanEntireNetwork(enabled: Boolean) = viewModelScope.launch { userPreferences.setLanScanEntireNetwork(enabled) }
    fun setLanExportFormat(format: String) = viewModelScope.launch { userPreferences.setLanExportFormat(format) }

    // --- Thermal Monitor Settings ---
    fun setThermalMonitorEnabled(enabled: Boolean) = viewModelScope.launch { 
        userPreferences.setThermalMonitorEnabled(enabled)
        if (enabled) {
            val interval = when (state.value.thermalMonitorInterval) {
                "30 Seconds" -> 1L
                "1 Minute" -> 1L
                "5 Minutes" -> 5L
                else -> 15L
            }
            com.example.netpulse.thermal.worker.ThermalWorker.schedule(getApplication(), interval)
        } else {
            com.example.netpulse.thermal.worker.ThermalWorker.cancel(getApplication())
        }
    }
    fun setThermalSaveHistory(enabled: Boolean) = viewModelScope.launch { userPreferences.setThermalSaveHistory(enabled) }
    fun setThermalNotifyHigh(enabled: Boolean) = viewModelScope.launch { userPreferences.setThermalNotifyHigh(enabled) }
    fun setThermalBackgroundMonitoring(enabled: Boolean) = viewModelScope.launch { userPreferences.setThermalBackgroundMonitoring(enabled) }
    fun setThermalMonitorWhileCharging(enabled: Boolean) = viewModelScope.launch { userPreferences.setThermalMonitorWhileCharging(enabled) }
    fun setThermalMonitorDuringSpeedTest(enabled: Boolean) = viewModelScope.launch { userPreferences.setThermalMonitorDuringSpeedTest(enabled) }
    fun setThermalHighThreshold(value: Float) = viewModelScope.launch { userPreferences.setThermalHighThreshold(value) }
    fun setThermalCriticalThreshold(value: Float) = viewModelScope.launch { userPreferences.setThermalCriticalThreshold(value) }

    fun onUpgradeTapped() = viewModelScope.launch { userPreferences.setIsPro(true) }
    fun onRestorePurchase() {}
    fun onRateApp(context: android.content.Context) = com.example.netpulse.utils.SettingsUtils.openPlayStore(context)
    fun onShareApp(context: android.content.Context) = com.example.netpulse.utils.SettingsUtils.shareApp(context)
    fun onPrivacyPolicy(context: android.content.Context) = com.example.netpulse.utils.SettingsUtils.openPrivacyPolicy(context)
}

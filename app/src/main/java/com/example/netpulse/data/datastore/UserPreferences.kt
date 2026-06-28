package com.example.netpulse.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "user_preferences"
)

class UserPreferences(private val context: Context) {

    companion object {
        val PARALLEL_CONNECTIONS = intPreferencesKey("parallel_connections")
        val TEST_DURATION_SECONDS = intPreferencesKey("test_duration_seconds")
        val AUTO_RUN_ON_WIFI = booleanPreferencesKey("auto_run_on_wifi")
        val IS_PRO = booleanPreferencesKey("is_pro")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        
        // Legacy/Additional keys from Prefs
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val PERSONALIZED_ADS = booleanPreferencesKey("personalized_ads")
        val AD_FREQUENCY = stringPreferencesKey("ad_frequency")
        val PRIVACY_POLICY_ACCEPTED = booleanPreferencesKey("privacy_policy_accepted")
        val DATA_USAGE_ACCEPTED = booleanPreferencesKey("data_usage_accepted")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val SPEED_DROP_ALERT_ENABLED = booleanPreferencesKey("speed_drop_alert_enabled")
        val AUTO_SCHEDULE_ENABLED = booleanPreferencesKey("auto_schedule_enabled")
        val BASELINE_SPEED = floatPreferencesKey("baseline_speed")
        val LAST_AUTO_TEST_TIME = longPreferencesKey("last_auto_test_time")
        val ADVERTISED_SPEED = floatPreferencesKey("advertised_speed")
        
        // Widget Settings
        val WIDGETS_ENABLED = booleanPreferencesKey("widgets_enabled")
        val WIDGET_REFRESH_INTERVAL = stringPreferencesKey("widget_refresh_interval")
        val WIDGET_AUTO_REFRESH_NETWORK = booleanPreferencesKey("widget_auto_refresh_network")
        val WIDGET_THEME = stringPreferencesKey("widget_theme")
        val WIDGET_TRANSPARENCY = floatPreferencesKey("widget_transparency")
        val WIDGET_ACCENT_COLOR = stringPreferencesKey("widget_accent_color")
        val WIDGET_SHOW_NETWORK_NAME = booleanPreferencesKey("widget_show_network_name")
        val WIDGET_SHOW_PUBLIC_IP = booleanPreferencesKey("widget_show_public_ip")
        val WIDGET_SHOW_ISP = booleanPreferencesKey("widget_show_isp")
        val WIDGET_SHOW_HEALTH_SCORE = booleanPreferencesKey("widget_show_health_score")
        val WIDGET_SHOW_LAST_UPDATED = booleanPreferencesKey("widget_show_last_updated")
        val WIDGET_OPEN_DESTINATION = stringPreferencesKey("widget_open_destination")
        
        // LAN Scanner Settings
        val LAN_SCANNER_ENABLED = booleanPreferencesKey("lan_scanner_enabled")
        val LAN_AUTO_SCAN_INTERVAL = stringPreferencesKey("lan_auto_scan_interval")
        val LAN_SCAN_ON_LAUNCH = booleanPreferencesKey("lan_scan_on_launch")
        val LAN_SCAN_ON_WIFI_CONNECTED = booleanPreferencesKey("lan_scan_on_wifi_connected")
        val LAN_SCAN_ON_NETWORK_CHANGE = booleanPreferencesKey("lan_scan_on_network_change")
        val LAN_NOTIFY_NEW_DEVICE = booleanPreferencesKey("lan_notify_new_device")
        val LAN_NOTIFY_DEVICE_LEAVES = booleanPreferencesKey("lan_notify_device_leaves")
        val LAN_NOTIFY_UNKNOWN_DEVICE = booleanPreferencesKey("lan_notify_unknown_device")
        val LAN_REMEMBER_NICKNAMES = booleanPreferencesKey("lan_remember_nicknames")
        val LAN_SAVE_HISTORY = booleanPreferencesKey("lan_save_history")
        val LAN_SHOW_OFFLINE_DEVICES = booleanPreferencesKey("lan_show_offline_devices")
        val LAN_SCAN_ENTIRE_NETWORK = booleanPreferencesKey("lan_scan_entire_network")
        val LAN_EXPORT_FORMAT = stringPreferencesKey("lan_export_format")

        // Thermal Monitor Settings
        val THERMAL_MONITOR_ENABLED = booleanPreferencesKey("thermal_monitor_enabled")
        val THERMAL_BACKGROUND_MONITORING = booleanPreferencesKey("thermal_background_monitoring")
        val THERMAL_MONITOR_WHILE_CHARGING = booleanPreferencesKey("thermal_monitor_while_charging")
        val THERMAL_MONITOR_DURING_SPEED_TEST = booleanPreferencesKey("thermal_monitor_during_speed_test")
        val THERMAL_MONITOR_DURING_GAMING = booleanPreferencesKey("thermal_monitor_during_gaming")
        val THERMAL_MONITOR_DURING_STREAMING = booleanPreferencesKey("thermal_monitor_during_streaming")
        val THERMAL_MONITOR_DURING_HEAVY_NETWORK = booleanPreferencesKey("thermal_monitor_during_heavy_network")
        val THERMAL_MONITOR_INTERVAL = stringPreferencesKey("thermal_monitor_interval")
        val THERMAL_HIGH_THRESHOLD = floatPreferencesKey("thermal_high_threshold")
        val THERMAL_CRITICAL_THRESHOLD = floatPreferencesKey("thermal_critical_threshold")
        val THERMAL_NOTIFY_HIGH = booleanPreferencesKey("thermal_notify_high")
        val THERMAL_NOTIFY_CRITICAL = booleanPreferencesKey("thermal_notify_critical")
        val THERMAL_NOTIFY_SOUND = booleanPreferencesKey("thermal_notify_sound")
        val THERMAL_NOTIFY_VIBRATION = booleanPreferencesKey("thermal_notify_vibration")
        val THERMAL_NOTIFY_RINGTONE = stringPreferencesKey("thermal_notify_ringtone")
        val THERMAL_NOTIFY_PRIORITY = stringPreferencesKey("thermal_notify_priority")
        val THERMAL_SAVE_HISTORY = booleanPreferencesKey("thermal_save_history")
        val THERMAL_MAX_HISTORY_DAYS = intPreferencesKey("thermal_max_history_days")
        val THERMAL_AUTO_DELETE_OLD = booleanPreferencesKey("thermal_auto_delete_old")
    }

    // LAN Scanner Flows
    val lanScannerEnabled: Flow<Boolean> = context.dataStore.data.map { it[LAN_SCANNER_ENABLED] ?: true }
    val lanAutoScanInterval: Flow<String> = context.dataStore.data.map { it[LAN_AUTO_SCAN_INTERVAL] ?: "Manual" }
    val lanScanOnLaunch: Flow<Boolean> = context.dataStore.data.map { it[LAN_SCAN_ON_LAUNCH] ?: false }
    val lanScanOnWifiConnected: Flow<Boolean> = context.dataStore.data.map { it[LAN_SCAN_ON_WIFI_CONNECTED] ?: false }
    val lanScanOnNetworkChange: Flow<Boolean> = context.dataStore.data.map { it[LAN_SCAN_ON_NETWORK_CHANGE] ?: false }
    val lanNotifyNewDevice: Flow<Boolean> = context.dataStore.data.map { it[LAN_NOTIFY_NEW_DEVICE] ?: false }
    val lanNotifyDeviceLeaves: Flow<Boolean> = context.dataStore.data.map { it[LAN_NOTIFY_DEVICE_LEAVES] ?: false }
    val lanNotifyUnknownDevice: Flow<Boolean> = context.dataStore.data.map { it[LAN_NOTIFY_UNKNOWN_DEVICE] ?: false }
    val lanRememberNicknames: Flow<Boolean> = context.dataStore.data.map { it[LAN_REMEMBER_NICKNAMES] ?: true }
    val lanSaveHistory: Flow<Boolean> = context.dataStore.data.map { it[LAN_SAVE_HISTORY] ?: true }
    val lanShowOfflineDevices: Flow<Boolean> = context.dataStore.data.map { it[LAN_SHOW_OFFLINE_DEVICES] ?: true }
    val lanScanEntireNetwork: Flow<Boolean> = context.dataStore.data.map { it[LAN_SCAN_ENTIRE_NETWORK] ?: true }
    val lanExportFormat: Flow<String> = context.dataStore.data.map { it[LAN_EXPORT_FORMAT] ?: "PDF" }

    suspend fun setLanScannerEnabled(enabled: Boolean) = context.dataStore.edit { it[LAN_SCANNER_ENABLED] = enabled }
    suspend fun setLanAutoScanInterval(interval: String) = context.dataStore.edit { it[LAN_AUTO_SCAN_INTERVAL] = interval }
    suspend fun setLanScanOnLaunch(enabled: Boolean) = context.dataStore.edit { it[LAN_SCAN_ON_LAUNCH] = enabled }
    suspend fun setLanScanOnWifiConnected(enabled: Boolean) = context.dataStore.edit { it[LAN_SCAN_ON_WIFI_CONNECTED] = enabled }
    suspend fun setLanScanOnNetworkChange(enabled: Boolean) = context.dataStore.edit { it[LAN_SCAN_ON_NETWORK_CHANGE] = enabled }
    suspend fun setLanNotifyNewDevice(enabled: Boolean) = context.dataStore.edit { it[LAN_NOTIFY_NEW_DEVICE] = enabled }
    suspend fun setLanNotifyDeviceLeaves(enabled: Boolean) = context.dataStore.edit { it[LAN_NOTIFY_DEVICE_LEAVES] = enabled }
    suspend fun setLanNotifyUnknownDevice(enabled: Boolean) = context.dataStore.edit { it[LAN_NOTIFY_UNKNOWN_DEVICE] = enabled }
    suspend fun setLanRememberNicknames(enabled: Boolean) = context.dataStore.edit { it[LAN_REMEMBER_NICKNAMES] = enabled }
    suspend fun setLanSaveHistory(enabled: Boolean) = context.dataStore.edit { it[LAN_SAVE_HISTORY] = enabled }
    suspend fun setLanShowOfflineDevices(enabled: Boolean) = context.dataStore.edit { it[LAN_SHOW_OFFLINE_DEVICES] = enabled }
    suspend fun setLanScanEntireNetwork(enabled: Boolean) = context.dataStore.edit { it[LAN_SCAN_ENTIRE_NETWORK] = enabled }
    suspend fun setLanExportFormat(format: String) = context.dataStore.edit { it[LAN_EXPORT_FORMAT] = format }

    // Thermal Monitor Flows
    val thermalMonitorEnabled: Flow<Boolean> = context.dataStore.data.map { it[THERMAL_MONITOR_ENABLED] ?: false }
    val thermalBackgroundMonitoring: Flow<Boolean> = context.dataStore.data.map { it[THERMAL_BACKGROUND_MONITORING] ?: true }
    val thermalMonitorWhileCharging: Flow<Boolean> = context.dataStore.data.map { it[THERMAL_MONITOR_WHILE_CHARGING] ?: true }
    val thermalMonitorDuringSpeedTest: Flow<Boolean> = context.dataStore.data.map { it[THERMAL_MONITOR_DURING_SPEED_TEST] ?: true }
    val thermalMonitorDuringGaming: Flow<Boolean> = context.dataStore.data.map { it[THERMAL_MONITOR_DURING_GAMING] ?: false }
    val thermalMonitorDuringStreaming: Flow<Boolean> = context.dataStore.data.map { it[THERMAL_MONITOR_DURING_STREAMING] ?: false }
    val thermalMonitorDuringHeavyNetwork: Flow<Boolean> = context.dataStore.data.map { it[THERMAL_MONITOR_DURING_HEAVY_NETWORK] ?: false }
    val thermalMonitorInterval: Flow<String> = context.dataStore.data.map { it[THERMAL_MONITOR_INTERVAL] ?: "5 Minutes" }
    val thermalHighThreshold: Flow<Float> = context.dataStore.data.map { it[THERMAL_HIGH_THRESHOLD] ?: 42f }
    val thermalCriticalThreshold: Flow<Float> = context.dataStore.data.map { it[THERMAL_CRITICAL_THRESHOLD] ?: 47f }
    val thermalNotifyHigh: Flow<Boolean> = context.dataStore.data.map { it[THERMAL_NOTIFY_HIGH] ?: true }
    val thermalNotifyCritical: Flow<Boolean> = context.dataStore.data.map { it[THERMAL_NOTIFY_CRITICAL] ?: true }
    val thermalNotifySound: Flow<Boolean> = context.dataStore.data.map { it[THERMAL_NOTIFY_SOUND] ?: true }
    val thermalNotifyVibration: Flow<Boolean> = context.dataStore.data.map { it[THERMAL_NOTIFY_VIBRATION] ?: true }
    val thermalNotifyRingtone: Flow<String> = context.dataStore.data.map { it[THERMAL_NOTIFY_RINGTONE] ?: "" }
    val thermalNotifyPriority: Flow<String> = context.dataStore.data.map { it[THERMAL_NOTIFY_PRIORITY] ?: "Default" }
    val thermalSaveHistory: Flow<Boolean> = context.dataStore.data.map { it[THERMAL_SAVE_HISTORY] ?: true }
    val thermalMaxHistoryDays: Flow<Int> = context.dataStore.data.map { it[THERMAL_MAX_HISTORY_DAYS] ?: 7 }
    val thermalAutoDeleteOld: Flow<Boolean> = context.dataStore.data.map { it[THERMAL_AUTO_DELETE_OLD] ?: true }

    suspend fun setThermalMonitorEnabled(enabled: Boolean) = context.dataStore.edit { it[THERMAL_MONITOR_ENABLED] = enabled }
    suspend fun setThermalBackgroundMonitoring(enabled: Boolean) = context.dataStore.edit { it[THERMAL_BACKGROUND_MONITORING] = enabled }
    suspend fun setThermalMonitorWhileCharging(enabled: Boolean) = context.dataStore.edit { it[THERMAL_MONITOR_WHILE_CHARGING] = enabled }
    suspend fun setThermalMonitorDuringSpeedTest(enabled: Boolean) = context.dataStore.edit { it[THERMAL_MONITOR_DURING_SPEED_TEST] = enabled }
    suspend fun setThermalMonitorDuringGaming(enabled: Boolean) = context.dataStore.edit { it[THERMAL_MONITOR_DURING_GAMING] = enabled }
    suspend fun setThermalMonitorDuringStreaming(enabled: Boolean) = context.dataStore.edit { it[THERMAL_MONITOR_DURING_STREAMING] = enabled }
    suspend fun setThermalMonitorDuringHeavyNetwork(enabled: Boolean) = context.dataStore.edit { it[THERMAL_MONITOR_DURING_HEAVY_NETWORK] = enabled }
    suspend fun setThermalMonitorInterval(interval: String) = context.dataStore.edit { it[THERMAL_MONITOR_INTERVAL] = interval }
    suspend fun setThermalHighThreshold(value: Float) = context.dataStore.edit { it[THERMAL_HIGH_THRESHOLD] = value }
    suspend fun setThermalCriticalThreshold(value: Float) = context.dataStore.edit { it[THERMAL_CRITICAL_THRESHOLD] = value }
    suspend fun setThermalNotifyHigh(enabled: Boolean) = context.dataStore.edit { it[THERMAL_NOTIFY_HIGH] = enabled }
    suspend fun setThermalNotifyCritical(enabled: Boolean) = context.dataStore.edit { it[THERMAL_NOTIFY_CRITICAL] = enabled }
    suspend fun setThermalNotifySound(enabled: Boolean) = context.dataStore.edit { it[THERMAL_NOTIFY_SOUND] = enabled }
    suspend fun setThermalNotifyVibration(enabled: Boolean) = context.dataStore.edit { it[THERMAL_NOTIFY_VIBRATION] = enabled }
    suspend fun setThermalNotifyRingtone(uri: String) = context.dataStore.edit { it[THERMAL_NOTIFY_RINGTONE] = uri }
    suspend fun setThermalNotifyPriority(priority: String) = context.dataStore.edit { it[THERMAL_NOTIFY_PRIORITY] = priority }
    suspend fun setThermalSaveHistory(enabled: Boolean) = context.dataStore.edit { it[THERMAL_SAVE_HISTORY] = enabled }
    suspend fun setThermalMaxHistoryDays(days: Int) = context.dataStore.edit { it[THERMAL_MAX_HISTORY_DAYS] = days }
    suspend fun setThermalAutoDeleteOld(enabled: Boolean) = context.dataStore.edit { it[THERMAL_AUTO_DELETE_OLD] = enabled }

    val widgetsEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[WIDGETS_ENABLED] ?: true }

    val widgetRefreshInterval: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[WIDGET_REFRESH_INTERVAL] ?: "1 Hour" }

    val widgetAutoRefreshNetwork: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[WIDGET_AUTO_REFRESH_NETWORK] ?: true }

    val widgetTheme: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[WIDGET_THEME] ?: "System" }

    val widgetTransparency: Flow<Float> = context.dataStore.data
        .map { prefs -> prefs[WIDGET_TRANSPARENCY] ?: 0f }

    val widgetAccentColor: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[WIDGET_ACCENT_COLOR] ?: "Blue" }

    val widgetShowNetworkName: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[WIDGET_SHOW_NETWORK_NAME] ?: true }

    val widgetShowPublicIp: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[WIDGET_SHOW_PUBLIC_IP] ?: true }

    val widgetShowIsp: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[WIDGET_SHOW_ISP] ?: true }

    val widgetShowHealthScore: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[WIDGET_SHOW_HEALTH_SCORE] ?: true }

    val widgetShowLastUpdated: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[WIDGET_SHOW_LAST_UPDATED] ?: true }

    val widgetOpenDestination: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[WIDGET_OPEN_DESTINATION] ?: "Home" }

    suspend fun setWidgetsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[WIDGETS_ENABLED] = enabled }
    }

    suspend fun setWidgetRefreshInterval(interval: String) {
        context.dataStore.edit { prefs -> prefs[WIDGET_REFRESH_INTERVAL] = interval }
    }

    suspend fun setWidgetAutoRefreshNetwork(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[WIDGET_AUTO_REFRESH_NETWORK] = enabled }
    }

    suspend fun setWidgetTheme(theme: String) {
        context.dataStore.edit { prefs -> prefs[WIDGET_THEME] = theme }
    }

    suspend fun setWidgetTransparency(transparency: Float) {
        context.dataStore.edit { prefs -> prefs[WIDGET_TRANSPARENCY] = transparency }
    }

    suspend fun setWidgetAccentColor(color: String) {
        context.dataStore.edit { prefs -> prefs[WIDGET_ACCENT_COLOR] = color }
    }

    suspend fun setWidgetShowNetworkName(show: Boolean) {
        context.dataStore.edit { prefs -> prefs[WIDGET_SHOW_NETWORK_NAME] = show }
    }

    suspend fun setWidgetShowPublicIp(show: Boolean) {
        context.dataStore.edit { prefs -> prefs[WIDGET_SHOW_PUBLIC_IP] = show }
    }

    suspend fun setWidgetShowIsp(show: Boolean) {
        context.dataStore.edit { prefs -> prefs[WIDGET_SHOW_ISP] = show }
    }

    suspend fun setWidgetShowHealthScore(show: Boolean) {
        context.dataStore.edit { prefs -> prefs[WIDGET_SHOW_HEALTH_SCORE] = show }
    }

    suspend fun setWidgetShowLastUpdated(show: Boolean) {
        context.dataStore.edit { prefs -> prefs[WIDGET_SHOW_LAST_UPDATED] = show }
    }

    suspend fun setWidgetOpenDestination(destination: String) {
        context.dataStore.edit { prefs -> prefs[WIDGET_OPEN_DESTINATION] = destination }
    }

    val languageCode: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[LANGUAGE_CODE] ?: "en" }
    
    suspend fun setLanguageCode(code: String) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_CODE] = code
        }
    }

    val parallelConnections: Flow<Int> = context.dataStore.data
        .map { prefs -> prefs[PARALLEL_CONNECTIONS] ?: 3 }

    val testDurationSeconds: Flow<Int> = context.dataStore.data
        .map { prefs -> prefs[TEST_DURATION_SECONDS] ?: 20 }

    val autoRunOnWifi: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[AUTO_RUN_ON_WIFI] ?: false }

    val isPro: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[IS_PRO] ?: false }

    val darkMode: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[DARK_MODE] ?: true }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[NOTIFICATIONS_ENABLED] ?: false }

    val onboardingDone: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[ONBOARDING_DONE] ?: false }

    val personalizedAds: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[PERSONALIZED_ADS] ?: true }

    val adFrequency: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[AD_FREQUENCY] ?: "Normal" }

    val speedDropAlertEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[SPEED_DROP_ALERT_ENABLED] ?: false }

    val autoScheduleEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[AUTO_SCHEDULE_ENABLED] ?: false }

    val baselineSpeed: Flow<Float> = context.dataStore.data
        .map { prefs -> prefs[BASELINE_SPEED] ?: 0f }

    val lastAutoTestTime: Flow<Long> = context.dataStore.data
        .map { prefs -> prefs[LAST_AUTO_TEST_TIME] ?: 0L }

    val advertisedSpeed: Flow<Float> = context.dataStore.data
        .map { prefs -> prefs[ADVERTISED_SPEED] ?: 100f }

    suspend fun setParallelConnections(value: Int) {
        context.dataStore.edit { prefs ->
            prefs[PARALLEL_CONNECTIONS] = value
        }
    }

    suspend fun setTestDuration(seconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[TEST_DURATION_SECONDS] = seconds
        }
    }

    suspend fun setAutoRunOnWifi(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_RUN_ON_WIFI] = enabled
        }
    }

    suspend fun setIsPro(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_PRO] = value
        }
    }

    suspend fun setDarkMode(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE] = value
        }
    }

    suspend fun setNotificationsEnabled(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = value
        }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_DONE] = done
        }
    }

    suspend fun setPersonalizedAds(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PERSONALIZED_ADS] = enabled
        }
    }

    suspend fun setAdFrequency(frequency: String) {
        context.dataStore.edit { prefs ->
            prefs[AD_FREQUENCY] = frequency
        }
    }

    suspend fun setSpeedDropAlertEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SPEED_DROP_ALERT_ENABLED] = enabled
        }
    }

    suspend fun setAutoScheduleEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_SCHEDULE_ENABLED] = enabled
        }
    }

    suspend fun setBaselineSpeed(speed: Float) {
        context.dataStore.edit { prefs ->
            prefs[BASELINE_SPEED] = speed
        }
    }

    suspend fun setLastAutoTestTime(time: Long) {
        context.dataStore.edit { prefs ->
            prefs[LAST_AUTO_TEST_TIME] = time
        }
    }

    suspend fun setAdvertisedSpeed(speed: Float) {
        context.dataStore.edit { prefs ->
            prefs[ADVERTISED_SPEED] = speed
        }
    }
}

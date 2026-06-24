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
        val DATA_PLAN_LIMIT_GB = intPreferencesKey("data_plan_limit_gb")
        val USAGE_TODAY_MB = floatPreferencesKey("usage_today_mb")
        val USAGE_WEEK_MB = floatPreferencesKey("usage_week_mb")
        val USAGE_MONTH_MB = floatPreferencesKey("usage_month_mb")
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

    val dataPlanLimitGb: Flow<Int> = context.dataStore.data
        .map { prefs -> prefs[DATA_PLAN_LIMIT_GB] ?: 100 }

    val usageTodayMb: Flow<Float> = context.dataStore.data
        .map { prefs -> prefs[USAGE_TODAY_MB] ?: 0f }

    val usageWeekMb: Flow<Float> = context.dataStore.data
        .map { prefs -> prefs[USAGE_WEEK_MB] ?: 0f }

    val usageMonthMb: Flow<Float> = context.dataStore.data
        .map { prefs -> prefs[USAGE_MONTH_MB] ?: 0f }

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

    suspend fun setDataPlanLimitGb(limit: Int) {
        context.dataStore.edit { prefs ->
            prefs[DATA_PLAN_LIMIT_GB] = limit
        }
    }

    suspend fun addUsage(mb: Float) {
        context.dataStore.edit { prefs ->
            prefs[USAGE_TODAY_MB] = (prefs[USAGE_TODAY_MB] ?: 0f) + mb
            prefs[USAGE_WEEK_MB] = (prefs[USAGE_WEEK_MB] ?: 0f) + mb
            prefs[USAGE_MONTH_MB] = (prefs[USAGE_MONTH_MB] ?: 0f) + mb
        }
    }

    suspend fun resetUsageToday() {
        context.dataStore.edit { prefs ->
            prefs[USAGE_TODAY_MB] = 0f
        }
    }
}

package com.example.netpulse.insights.usage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "estimated_usage")

class UsageRepository(private val context: Context) {
    private val TODAY_KEY = floatPreferencesKey("today_mb")
    private val WEEK_KEY = floatPreferencesKey("week_mb")
    private val MONTH_KEY = floatPreferencesKey("month_mb")
    private val LIMIT_KEY = floatPreferencesKey("plan_limit_gb")

    val todayMB: Flow<Float> = context.dataStore.data.map { it[TODAY_KEY] ?: 0f }
    val weekMB: Flow<Float> = context.dataStore.data.map { it[WEEK_KEY] ?: 0f }
    val monthMB: Flow<Float> = context.dataStore.data.map { it[MONTH_KEY] ?: 0f }
    val planLimitGB: Flow<Float> = context.dataStore.data.map { it[LIMIT_KEY] ?: 100f }

    suspend fun addUsage(downMbps: Double, upMbps: Double) {
        val duration = 20f // Standard 20s for engine
        val mbUsed = (duration * (downMbps + upMbps).toFloat()) / 8f
        
        context.dataStore.edit { prefs ->
            prefs[TODAY_KEY] = (prefs[TODAY_KEY] ?: 0f) + mbUsed
            prefs[WEEK_KEY] = (prefs[WEEK_KEY] ?: 0f) + mbUsed
            prefs[MONTH_KEY] = (prefs[MONTH_KEY] ?: 0f) + mbUsed
        }
    }

    suspend fun resetToday() {
        context.dataStore.edit { prefs -> prefs[TODAY_KEY] = 0f }
    }
}

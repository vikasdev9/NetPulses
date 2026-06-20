package com.example.netpulse.widget

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.netpulse.data.SpeedResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "widget_prefs")

enum class WidgetState {
    NO_DATA, HAS_DATA, LOADING, ERROR
}

data class WidgetData(
    val downloadMbps: Double = 0.0,
    val uploadMbps: Double = 0.0,
    val pingMs: Int = 0,
    val jitterMs: Int = 0,
    val networkType: String = "—",
    val isp: String = "—",
    val lastTestedLabel: String = "—",
    val state: WidgetState = WidgetState.NO_DATA
)

object WidgetDataStore {
    private val DOWNLOAD = doublePreferencesKey("download")
    private val UPLOAD = doublePreferencesKey("upload")
    private val PING = intPreferencesKey("ping")
    private val JITTER = intPreferencesKey("jitter")
    private val NETWORK = stringPreferencesKey("network")
    private val ISP = stringPreferencesKey("isp")
    private val LABEL = stringPreferencesKey("label")
    private val STATE = stringPreferencesKey("state")

    suspend fun saveWidgetData(context: Context, result: SpeedResult) {
        context.dataStore.edit { prefs ->
            prefs[DOWNLOAD] = result.downloadMbps
            prefs[UPLOAD] = result.uploadMbps
            prefs[PING] = result.pingMs
            prefs[JITTER] = result.jitterMs
            prefs[NETWORK] = result.networkType
            prefs[ISP] = result.isp
            prefs[LABEL] = java.text.SimpleDateFormat("h:mm a", java.util.Locale.US).format(java.util.Date(result.timestamp))
            prefs[STATE] = WidgetState.HAS_DATA.name
        }
    }

    suspend fun loadWidgetData(context: Context): WidgetData {
        return context.dataStore.data.map { prefs ->
            WidgetData(
                downloadMbps = prefs[DOWNLOAD] ?: 0.0,
                uploadMbps = prefs[UPLOAD] ?: 0.0,
                pingMs = prefs[PING] ?: 0,
                jitterMs = prefs[JITTER] ?: 0,
                networkType = prefs[NETWORK] ?: "—",
                isp = prefs[ISP] ?: "—",
                lastTestedLabel = prefs[LABEL] ?: "—",
                state = WidgetState.valueOf(prefs[STATE] ?: WidgetState.NO_DATA.name)
            )
        }.first()
    }

    // Support existing updateData if needed, but saveWidgetData is preferred
    suspend fun updateData(context: Context, data: WidgetData) {
        context.dataStore.edit { prefs ->
            prefs[DOWNLOAD] = data.downloadMbps
            prefs[UPLOAD] = data.uploadMbps
            prefs[PING] = data.pingMs
            prefs[JITTER] = data.jitterMs
            prefs[NETWORK] = data.networkType
            prefs[ISP] = data.isp
            prefs[LABEL] = data.lastTestedLabel
            prefs[STATE] = data.state.name
        }
    }

    suspend fun getData(context: Context): WidgetData = loadWidgetData(context)
}

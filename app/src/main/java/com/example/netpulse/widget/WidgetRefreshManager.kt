package com.example.netpulse.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.example.netpulse.data.datastore.UserPreferences
import com.example.netpulse.worker.WidgetUpdateWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object WidgetRefreshManager {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun refreshAllWidgets(context: Context) {
        scope.launch {
            val userPreferences = UserPreferences(context)
            val theme = userPreferences.widgetTheme.first()
            
            // Sync preferences to WidgetDataStore
            val currentData = WidgetDataStore.loadWidgetData(context)
            WidgetDataStore.updateData(context, currentData.copy(theme = theme))

            NetPulseWidget().updateAll(context)
            QuickSpeedTestWidget().updateAll(context)
            LiveSpeedWidget().updateAll(context)
            InternetIntelligenceWidget().updateAll(context)
            PerformanceDashboardWidget().updateAll(context)
            MiniAnalyticsWidget().updateAll(context)
        }
    }

    fun updateRefreshInterval(context: Context, intervalString: String) {
        if (intervalString == "Manual") {
            WidgetUpdateWorker.cancel(context)
        } else {
            val minutes = when (intervalString) {
                "15 Minutes" -> 15L
                "30 Minutes" -> 30L
                "1 Hour" -> 60L
                "3 Hours" -> 180L
                "6 Hours" -> 360L
                "12 Hours" -> 720L
                "24 Hours" -> 1440L
                else -> 60L
            }
            WidgetUpdateWorker.schedule(context, minutes)
        }
    }
}

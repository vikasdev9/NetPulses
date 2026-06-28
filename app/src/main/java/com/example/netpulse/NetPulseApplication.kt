package com.example.netpulse

import android.app.Application
import android.content.Context
import com.example.netpulse.data.NetPulseDatabase
import com.example.netpulse.utils.LocaleUtils
import com.example.netpulse.utils.NotificationHelper
import com.example.netpulse.insights.wifistability.WifiStabilityWorker
import com.example.netpulse.insights.usage.DailyResetWorker
import com.example.netpulse.thermal.worker.ThermalWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NetPulseApplication : Application() {
    val database by lazy { NetPulseDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        WifiStabilityWorker.schedule(this)
        DailyResetWorker.schedule(this)
        
        // Initialize Thermal Monitor if enabled
        val userPreferences = com.example.netpulse.data.datastore.UserPreferences(this)
        CoroutineScope(Dispatchers.Main).launch {
            val enabled = userPreferences.thermalMonitorEnabled.first()
            if (enabled) {
                 ThermalWorker.schedule(this@NetPulseApplication, 15)
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        val lang = LocaleUtils.getSavedLanguage(base)
        if (lang.isNotEmpty()) {
            super.attachBaseContext(LocaleUtils.setLocale(base, lang))
        } else {
            super.attachBaseContext(base)
        }
    }
}

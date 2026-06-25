package com.example.netpulse

import android.app.Application
import android.content.Context
import com.example.netpulse.data.NetPulseDatabase
import com.example.netpulse.utils.LocaleUtils
import com.example.netpulse.utils.NotificationHelper
import com.example.netpulse.insights.wifistability.WifiStabilityWorker
import com.example.netpulse.insights.usage.DailyResetWorker

class NetPulseApplication : Application() {
    val database by lazy { NetPulseDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        WifiStabilityWorker.schedule(this)
        DailyResetWorker.schedule(this)
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

package com.example.netpulse

import android.app.Application
import com.example.netpulse.data.NetPulseDatabase
import com.example.netpulse.utils.NotificationHelper

class NetPulseApplication : Application() {
    val database by lazy { NetPulseDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }
}

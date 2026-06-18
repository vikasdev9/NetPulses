package com.example.netpulse.widget

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class NetPulseWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = NetPulseWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_RUN_TEST) {
            val serviceIntent = Intent(context, WidgetSpeedTestService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    companion object {
        const val ACTION_RUN_TEST = "com.netpulse.widget.ACTION_RUN_TEST"
    }
}

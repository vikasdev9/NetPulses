package com.example.netpulse.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class LiveSpeedWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = LiveSpeedWidget()
}

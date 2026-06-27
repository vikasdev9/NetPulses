package com.example.netpulse.utils

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build

object WidgetPinningManager {
    fun pinWidget(context: Context, receiverClass: Class<*>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
            val myProvider = ComponentName(context, receiverClass)

            if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
                val successCallback = PendingIntent.getBroadcast(
                    context,
                    0,
                    Intent(context, receiverClass),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
            }
        }
    }
}

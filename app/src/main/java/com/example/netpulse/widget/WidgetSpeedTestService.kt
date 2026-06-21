package com.example.netpulse.widget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.glance.appwidget.updateAll
import com.example.netpulse.R
import com.example.netpulse.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WidgetSpeedTestService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(1, buildNotification())
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateWidgetState(WidgetState.LOADING)
                
                // Simulate Speed Test logic
                delay(3000) 
                
                val download = (50..120).random().toDouble() + Math.random()
                val upload = (20..60).random().toDouble() + Math.random()

                val data = WidgetData(
                    downloadMbps = download,
                    uploadMbps = upload,
                    pingMs = (10..22).random(),
                    jitterMs = (2..6).random(),
                    networkType = "WiFi",
                    isp = "Jio",
                    lastTestedLabel = SimpleDateFormat("h:mm a", Locale.US).format(Date()),
                    state = WidgetState.HAS_DATA
                )
                
                WidgetDataStore.updateData(applicationContext, data)
                NetPulseWidget().updateAll(applicationContext)

                // Fire notification
                NotificationHelper.showWidgetTestComplete(applicationContext, download, upload)

            } catch (e: Exception) {
                updateWidgetState(WidgetState.ERROR)
            } finally {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun updateWidgetState(state: WidgetState) {
        val current = WidgetDataStore.getData(applicationContext)
        WidgetDataStore.updateData(applicationContext, current.copy(state = state))
        NetPulseWidget().updateAll(applicationContext)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "widget_test_channel",
                "Widget Speed Test",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, "widget_test_channel")
            .setContentTitle("NetPulse")
            .setContentText("Testing network speed...")
            .setSmallIcon(R.drawable.ic_bolt)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

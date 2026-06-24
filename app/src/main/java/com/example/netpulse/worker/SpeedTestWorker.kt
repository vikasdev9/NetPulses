package com.example.netpulse.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.netpulse.NetPulseApplication
import com.example.netpulse.data.SpeedResult
import com.example.netpulse.data.datastore.UserPreferences
import com.example.netpulse.data.network.SpeedTestEngine
import com.example.netpulse.utils.NotificationHelper
import kotlinx.coroutines.flow.first

class SpeedTestWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val application = applicationContext as NetPulseApplication
        val userPreferences = UserPreferences(applicationContext)
        val dao = application.database.speedResultDao()

        return try {
            val (ping, jitter) = SpeedTestEngine.measurePing()
            val download = SpeedTestEngine.measureDownload(3, 10) { }
            val upload = SpeedTestEngine.measureUpload(3, 10) { }

            val result = SpeedResult(
                timestamp = System.currentTimeMillis(),
                downloadMbps = download,
                uploadMbps = upload,
                pingMs = ping.toInt(),
                jitterMs = jitter.toInt(),
                networkType = "Auto Test",
                isp = "Auto",
                ipAddress = "0.0.0.0",
                location = "Background"
            )

            dao.insert(result)
            userPreferences.setLastAutoTestTime(System.currentTimeMillis())

            val baseline = userPreferences.baselineSpeed.first()
            val alertEnabled = userPreferences.speedDropAlertEnabled.first()

            if (alertEnabled && baseline > 0 && download < (baseline * 0.5)) {
                NotificationHelper.showSpeedDropAlert(applicationContext, download, baseline)
            }

            // Update baseline if this speed is higher (common practice to keep baseline fresh)
            if (download > baseline) {
                userPreferences.setBaselineSpeed(download.toFloat())
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

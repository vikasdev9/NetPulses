package com.example.netpulse.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.netpulse.NetPulseApplication
import com.example.netpulse.data.SpeedResult
import com.example.netpulse.data.datastore.UserPreferences
import com.example.netpulse.data.network.SpeedTestEngine
import com.example.netpulse.utils.IspInfo
import com.example.netpulse.utils.IspInfoHelper
import com.example.netpulse.utils.NotificationHelper
import com.example.netpulse.widget.NetPulseWidget
import com.example.netpulse.widget.WidgetDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.glance.appwidget.updateAll
import java.util.Calendar

enum class TestPhase {
    PING, JITTER, DOWNLOAD, UPLOAD
}

sealed class SpeedTestState {
    object Idle : SpeedTestState()
    data class Running(
        val phase: TestPhase,
        val currentSpeed: Float = 0f,
        val ping: Float? = null,
        val jitter: Float? = null,
        val download: Float? = null
    ) : SpeedTestState()
    data class Complete(val result: SpeedResult) : SpeedTestState()
    data class Error(val message: String) : SpeedTestState()
}

data class SpeedTestUiState(
    val testState: SpeedTestState = SpeedTestState.Idle,
    val download: String = "—",
    val upload: String = "—",
    val ping: String = "—",
    val jitter: String = "—",
    val isTestRunning: Boolean = false,
    val instantSamples: List<Float> = emptyList(),
    val hasResults: Boolean = false,
    val progress: Float = 0f,
    val currentTest: String = "",
    val networkType: String = "WiFi",
    val downloadSpeed: Float = 0f,
    val uploadSpeed: Float = 0f,
    val jitterValue: Float = 0f,
    val minSpeed: Float = 0f,
    val avgSpeed: Float = 0f,
    val maxSpeed: Float = 0f,
    val packetLoss: Float = 0f,
    val errorMessage: String? = null
)

class SpeedTestViewModel(
    application: Application,
    private val userPreferences: UserPreferences
) : AndroidViewModel(application) {

    private val dao = (application as NetPulseApplication).database.speedResultDao()

    private val _uiState = MutableStateFlow(SpeedTestUiState())
    val uiState: StateFlow<SpeedTestUiState> = _uiState.asStateFlow()

    private val _ispInfo = MutableStateFlow(IspInfo())
    val ispInfo: StateFlow<IspInfo> = _ispInfo.asStateFlow()

    private var testJob: Job? = null

    init {
        viewModelScope.launch {
            _ispInfo.value = IspInfoHelper.fetchIspInfo(application)
        }
    }

    fun startTest() {
        startSpeedTest()
    }

    fun startSpeedTest() {
        testJob?.cancel()
        testJob = viewModelScope.launch {
            
            val parallelConnections = userPreferences.parallelConnections.first()
            val durationSeconds = userPreferences.testDurationSeconds.first()
            val notificationsEnabled = userPreferences.notificationsEnabled.first()

            _uiState.value = SpeedTestUiState(
                testState = SpeedTestState.Running(TestPhase.PING),
                isTestRunning = true,
                currentTest = "PING"
            )

            val (ping, jitter) = SpeedTestEngine.measurePing()
            if (ping < 0) {
                _uiState.value = _uiState.value.copy(
                    testState = SpeedTestState.Error("Ping failed"),
                    errorMessage = "Ping failed"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                ping = "%.0f".format(ping),
                jitter = "%.1f".format(jitter),
                jitterValue = jitter.toFloat(),
                testState = SpeedTestState.Running(TestPhase.DOWNLOAD, ping = ping.toFloat(), jitter = jitter.toFloat()),
                currentTest = "DOWNLOAD",
                progress = 0.33f
            )

            val downloadSamples = mutableListOf<Float>()
            val download = SpeedTestEngine.measureDownload(
                parallelConnections = parallelConnections,
                durationSeconds = durationSeconds
            ) { speed ->
                val floatSpeed = speed.toFloat()
                downloadSamples.add(floatSpeed)
                _uiState.value = _uiState.value.copy(
                    download = "%.1f".format(floatSpeed),
                    testState = SpeedTestState.Running(TestPhase.DOWNLOAD, floatSpeed, ping.toFloat(), jitter.toFloat()),
                    instantSamples = downloadSamples.toList(),
                    minSpeed = downloadSamples.minOrNull() ?: 0f,
                    maxSpeed = downloadSamples.maxOrNull() ?: 0f,
                    avgSpeed = downloadSamples.average().toFloat(),
                    progress = 0.33f + (0.33f * (downloadSamples.size / 50f).coerceAtMost(1f))
                )
            }

            if (download < 0) {
                _uiState.value = _uiState.value.copy(
                    testState = SpeedTestState.Error("Download failed"),
                    errorMessage = "Download failed"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                download = "%.1f".format(download),
                downloadSpeed = download.toFloat(),
                testState = SpeedTestState.Running(TestPhase.UPLOAD, ping = ping.toFloat(), jitter = jitter.toFloat(), download = download.toFloat()),
                currentTest = "UPLOAD",
                progress = 0.66f,
                instantSamples = emptyList()
            )

            val uploadSamples = mutableListOf<Float>()
            val upload = SpeedTestEngine.measureUpload(
                parallelConnections = parallelConnections,
                durationSeconds = durationSeconds
            ) { speed ->
                val floatSpeed = speed.toFloat()
                uploadSamples.add(floatSpeed)
                _uiState.value = _uiState.value.copy(
                    upload = "%.1f".format(floatSpeed),
                    testState = SpeedTestState.Running(TestPhase.UPLOAD, floatSpeed, ping.toFloat(), jitter.toFloat(), download.toFloat()),
                    instantSamples = uploadSamples.toList(),
                    progress = 0.66f + (0.34f * (uploadSamples.size / 50f).coerceAtMost(1f))
                )
            }

            if (upload < 0) {
                _uiState.value = _uiState.value.copy(
                    testState = SpeedTestState.Error("Upload failed"),
                    errorMessage = "Upload failed"
                )
                return@launch
            }

            val result = SpeedResult(
                timestamp = System.currentTimeMillis(),
                downloadMbps = download,
                uploadMbps = upload,
                pingMs = ping.toInt(),
                jitterMs = jitter.toInt(),
                networkType = _uiState.value.networkType,
                isp = _ispInfo.value.isp,
                ipAddress = _ispInfo.value.ip,
                location = "Closest Edge"
            )

            withContext(Dispatchers.IO) {
                dao.insert(result)
                
                if (notificationsEnabled) {
                    NotificationHelper.showTestCompleteNotification(getApplication(), download, upload, ping.toInt())
                    
                    if (download < 5.0) {
                        NotificationHelper.showSlowInternetWarning(getApplication(), download)
                    }

                    // Check for speed drop
                    val sevenDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.timeInMillis
                    val avgSpeed = dao.getAverageDownloadSpeedAfter(sevenDaysAgo)
                    if (avgSpeed != null && download < (avgSpeed * 0.5)) {
                        NotificationHelper.showSpeedDropAlert(getApplication(), download, avgSpeed)
                    }
                }
            }

            WidgetDataStore.saveWidgetData(getApplication(), result)
            NetPulseWidget().updateAll(getApplication())

            val overshootValue = (download * 1.12).toFloat().coerceAtMost(100f)
            _uiState.value = _uiState.value.copy(
                testState = SpeedTestState.Running(TestPhase.DOWNLOAD, overshootValue, ping.toFloat(), jitter.toFloat())
            )
            delay(300)

            _uiState.value = _uiState.value.copy(
                upload = "%.1f".format(upload),
                uploadSpeed = upload.toFloat(),
                testState = SpeedTestState.Complete(result),
                isTestRunning = false,
                hasResults = true,
                progress = 1f,
                currentTest = "COMPLETED"
            )
        }
    }

    fun stopTest() {
        testJob?.cancel()
        _uiState.value = SpeedTestUiState()
    }
    
    fun resetTest() = stopTest()

    class Factory(
        private val application: Application,
        private val userPreferences: UserPreferences
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SpeedTestViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SpeedTestViewModel(application, userPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

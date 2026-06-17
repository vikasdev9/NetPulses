package com.example.netpulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class TestPhase {
    PING, JITTER, DOWNLOAD, UPLOAD
}

sealed class SpeedTestState {
    object Idle : SpeedTestState()
    data class Running(val phase: TestPhase, val currentSpeed: Float = 0f) : SpeedTestState()
    data class Complete(val download: Float, val upload: Float, val ping: Float, val jitter: Float) : SpeedTestState()
}

data class SpeedTestUiState(
    val testState: SpeedTestState = SpeedTestState.Idle,
    val download: String = "—",
    val upload: String = "—",
    val ping: String = "—",
    val jitter: String = "—",
    
    // Compatibility fields for other screens if needed
    val isTestRunning: Boolean = false,
    val instantSamples: List<Float> = emptyList(),
    val hasResults: Boolean = false,
    val downloadSpeed: Float = 0f,
    val uploadSpeed: Float = 0f,
    val jitterValue: Float = 0f,
    val progress: Float = 0f,
    val currentTest: String = "",
    val minSpeed: Float = 0f,
    val avgSpeed: Float = 0f,
    val maxSpeed: Float = 0f,
    val packetLoss: Float = 0f,
    val networkType: String = "WiFi",
    val errorMessage: String? = null
)

class SpeedTestViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SpeedTestUiState())
    val uiState = _uiState.asStateFlow()

    private var testJob: Job? = null

    fun startTest() {
        testJob?.cancel()
        testJob = viewModelScope.launch {
            // Reset
            _uiState.value = SpeedTestUiState(
                testState = SpeedTestState.Running(TestPhase.PING),
                isTestRunning = true,
                currentTest = "PING"
            )
            
            // 1. Ping
            delay(1000)
            val finalPing = 12 + Random.nextInt(10)
            _uiState.value = _uiState.value.copy(
                ping = "$finalPing",
                testState = SpeedTestState.Running(TestPhase.JITTER),
                currentTest = "JITTER",
                progress = 0.2f
            )

            // 2. Jitter
            delay(1000)
            val finalJitter = 1.5f + Random.nextFloat() * 2f
            _uiState.value = _uiState.value.copy(
                jitter = "%.1f".format(finalJitter),
                jitterValue = finalJitter,
                testState = SpeedTestState.Running(TestPhase.DOWNLOAD),
                currentTest = "DOWNLOAD",
                progress = 0.4f
            )

            // 3. Download
            val samples = mutableListOf<Float>()
            for (i in 1..20) {
                val currentSample = 90f + Random.nextFloat() * 20f
                samples.add(currentSample)
                _uiState.value = _uiState.value.copy(
                    download = "%.1f".format(currentSample),
                    testState = SpeedTestState.Running(TestPhase.DOWNLOAD, currentSample),
                    progress = 0.4f + (i / 20f) * 0.3f,
                    instantSamples = samples.toList(),
                    minSpeed = samples.minOrNull() ?: 0f,
                    maxSpeed = samples.maxOrNull() ?: 0f,
                    avgSpeed = samples.average().toFloat()
                )
                delay(100)
            }
            val finalDownload = samples.average().toFloat()
            _uiState.value = _uiState.value.copy(
                download = "%.1f".format(finalDownload),
                downloadSpeed = finalDownload,
                testState = SpeedTestState.Running(TestPhase.UPLOAD),
                currentTest = "UPLOAD"
            )

            // 4. Upload
            val uploadSamples = mutableListOf<Float>()
            for (i in 1..20) {
                val currentSample = 40f + Random.nextFloat() * 10f
                uploadSamples.add(currentSample)
                _uiState.value = _uiState.value.copy(
                    upload = "%.1f".format(currentSample),
                    testState = SpeedTestState.Running(TestPhase.UPLOAD, currentSample),
                    progress = 0.7f + (i / 20f) * 0.3f
                )
                delay(100)
            }
            val finalUpload = uploadSamples.average().toFloat()

            // Complete
            _uiState.value = _uiState.value.copy(
                upload = "%.1f".format(finalUpload),
                uploadSpeed = finalUpload,
                testState = SpeedTestState.Complete(
                    download = finalDownload,
                    upload = finalUpload,
                    ping = finalPing.toFloat(),
                    jitter = finalJitter
                ),
                isTestRunning = false,
                hasResults = true,
                progress = 1f,
                currentTest = "COMPLETED",
                packetLoss = 0.1f
            )
        }
    }

    fun stopTest() {
        testJob?.cancel()
        _uiState.value = SpeedTestUiState()
    }

    // Aliases for compatibility
    fun startSpeedTest() = startTest()
    fun resetTest() = stopTest()
}

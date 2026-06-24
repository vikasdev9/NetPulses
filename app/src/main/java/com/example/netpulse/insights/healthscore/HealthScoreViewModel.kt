package com.example.netpulse.insights.healthscore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netpulse.data.SpeedResult
import com.example.netpulse.data.db.SpeedResultDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class HealthScoreState(
    val score: Int = 0,
    val label: String = "Unknown",
    val trend: Int = 0, // 1: Up, -1: Down, 0: Neutral
    val isLoading: Boolean = true
)

class HealthScoreViewModel(private val dao: SpeedResultDao) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthScoreState())
    val uiState: StateFlow<HealthScoreState> = _uiState.asStateFlow()

    init {
        calculateHealthScore()
    }

    private fun calculateHealthScore() {
        viewModelScope.launch {
            dao.getAll().collect { results ->
                if (results.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@collect
                }

                val latest = results.first()
                val todayScore = computeScore(latest)

                // Get yesterday's average score for trend
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                val startOfYesterday = calendar.timeInMillis
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                val endOfYesterday = calendar.timeInMillis

                val yesterdayResults = results.filter { it.timestamp in startOfYesterday..endOfYesterday }
                val yesterdayAvgScore = if (yesterdayResults.isNotEmpty()) {
                    yesterdayResults.map { computeScore(it) }.average().toInt()
                } else 0

                val trend = when {
                    todayScore > yesterdayAvgScore -> 1
                    todayScore < yesterdayAvgScore -> -1
                    else -> 0
                }

                _uiState.update {
                    it.copy(
                        score = todayScore,
                        label = getLabel(todayScore),
                        trend = trend,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun computeScore(result: SpeedResult): Int {
        val downloadScore = (result.downloadMbps / 100.0 * 100.0).coerceAtMost(100.0)
        val uploadScore = (result.uploadMbps / 50.0 * 100.0).coerceAtMost(100.0)
        val pingScore = (10.0 / result.pingMs.toDouble().coerceAtLeast(1.0) * 100.0).coerceAtMost(100.0)
        val jitterScore = (1.0 / result.jitterMs.toDouble().coerceAtLeast(0.1) * 100.0).coerceAtMost(100.0)

        return (downloadScore * 0.4 + uploadScore * 0.2 + pingScore * 0.25 + jitterScore * 0.15).toInt()
    }

    private fun getLabel(score: Int): String = when {
        score <= 40 -> "Poor"
        score <= 70 -> "Fair"
        score <= 90 -> "Good"
        else -> "Excellent"
    }
}

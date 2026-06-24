package com.example.netpulse.insights.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netpulse.data.db.SpeedResultDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class TrendPoint(val label: String, val value: Double)

data class TrendsState(
    val points: List<TrendPoint> = emptyList(),
    val highest: Double = 0.0,
    val lowest: Double = 0.0,
    val average: Double = 0.0,
    val isMonthly: Boolean = false,
    val isLoading: Boolean = true
)

class TrendsViewModel(private val dao: SpeedResultDao) : ViewModel() {

    private val _uiState = MutableStateFlow(TrendsState())
    val uiState: StateFlow<TrendsState> = _uiState.asStateFlow()

    init {
        loadTrends(false)
    }

    fun setPeriod(isMonthly: Boolean) {
        _uiState.update { it.copy(isMonthly = isMonthly, isLoading = true) }
        loadTrends(isMonthly)
    }

    private fun loadTrends(isMonthly: Boolean) {
        viewModelScope.launch {
            dao.getAll().collect { allResults ->
                val daysToLookBack = if (isMonthly) 30 else 7
                val calendar = Calendar.getInstance()
                val points = mutableListOf<TrendPoint>()
                
                val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

                for (i in (daysToLookBack - 1) downTo 0) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -i)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    val start = cal.timeInMillis
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    val end = cal.timeInMillis
                    
                    val dayResults = allResults.filter { it.timestamp in start..end }
                    val avg = if (dayResults.isNotEmpty()) dayResults.map { it.downloadMbps }.average() else 0.0
                    
                    points.add(TrendPoint(dateFormat.format(cal.time), avg))
                }

                _uiState.update {
                    it.copy(
                        points = points,
                        highest = if (points.isNotEmpty()) points.maxOf { p -> p.value } else 0.0,
                        lowest = if (points.isNotEmpty()) points.filter { p -> p.value > 0 }.minOfOrNull { p -> p.value } ?: 0.0 else 0.0,
                        average = if (points.isNotEmpty()) points.filter { p -> p.value > 0 }.map { p -> p.value }.average().takeIf { a -> !a.isNaN() } ?: 0.0 else 0.0,
                        isLoading = false
                    )
                }
            }
        }
    }
}

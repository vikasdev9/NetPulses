package com.example.netpulse.insights.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netpulse.data.SpeedResult
import com.example.netpulse.data.db.SpeedResultDao
import com.example.netpulse.data.datastore.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Priority { HIGH, MEDIUM, LOW }

data class Recommendation(
    val id: String,
    val title: String,
    val description: String,
    val priority: Priority
)

class RecommendationsViewModel(
    private val speedDao: SpeedResultDao,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _recommendations = MutableStateFlow<List<Recommendation>>(emptyList())
    val recommendations: StateFlow<List<Recommendation>> = _recommendations.asStateFlow()

    init {
        generateRecommendations()
    }

    private fun generateRecommendations() {
        viewModelScope.launch {
            combine(
                speedDao.getAll(),
                userPreferences.baselineSpeed,
                userPreferences.dataPlanLimitGb // advertised placeholder
            ) { results, baseline, advertised ->
                val list = mutableListOf<Recommendation>()
                if (results.isEmpty()) return@combine emptyList<Recommendation>()
                
                val latest = results.first()
                
                // Rule 2: Ping
                if (latest.pingMs > 50) {
                    list.add(Recommendation("high_ping", "High Latency Detected", "Try connecting via ethernet or restart your router.", Priority.MEDIUM))
                }
                
                // Rule 3: Jitter
                if (latest.jitterMs > 10) {
                    list.add(Recommendation("high_jitter", "Unstable Connection", "Avoid video calls during peak hours.", Priority.MEDIUM))
                }
                
                // Rule 5: Upload
                if (latest.uploadMbps < latest.downloadMbps * 0.1) {
                    list.add(Recommendation("low_upload", "Low Upload Speed", "This affects video calls and cloud backup performance.", Priority.LOW))
                }
                
                // Rule 7: Health Score logic (omitted complex check for now, can use previous score)
                
                list
            }.collect {
                _recommendations.value = it
            }
        }
    }
}

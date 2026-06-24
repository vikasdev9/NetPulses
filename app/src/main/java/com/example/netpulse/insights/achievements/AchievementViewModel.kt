package com.example.netpulse.insights.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netpulse.data.db.SpeedResultDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AchievementState(
    val achievements: List<AchievementEntity> = emptyList(),
    val streak: Int = 0,
    val showConfetti: Boolean = false,
    val isLoading: Boolean = true
)

class AchievementViewModel(
    private val achievementDao: AchievementDao,
    private val speedDao: SpeedResultDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementState())
    val uiState: StateFlow<AchievementState> = _uiState.asStateFlow()

    init {
        loadAchievements()
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            achievementDao.getAll().collect { list ->
                if (list.isEmpty()) {
                    initializeAchievements()
                } else {
                    _uiState.update { it.copy(achievements = list, isLoading = false) }
                }
            }
        }
    }

    private suspend fun initializeAchievements() {
        val initial = listOf(
            AchievementEntity("first_test", "First Test", "Run your first speed test"),
            AchievementEntity("streak_3", "3 Day Streak", "Test 3 consecutive days"),
            AchievementEntity("streak_7", "7 Day Streak", "Test 7 consecutive days"),
            AchievementEntity("speed_demon", "Speed Demon", "Record download > 100 Mbps"),
            AchievementEntity("low_latency", "Low Latency", "Ping under 10ms")
        )
        achievementDao.insertAll(initial)
    }

    fun dismissConfetti() {
        _uiState.update { it.copy(showConfetti = false) }
    }
}

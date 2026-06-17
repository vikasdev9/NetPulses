package com.example.netpulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.netpulse.data.SpeedResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HistoryViewModel : ViewModel() {
    private val _historyItems = MutableStateFlow<List<SpeedResult>>(getSampleData())
    val historyItems = _historyItems.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter = _selectedFilter.asStateFlow()

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun deleteItem(item: SpeedResult) {
        _historyItems.update { currentList ->
            currentList.filter { it.id != item.id }
        }
    }

    fun clearAll() {
        _historyItems.value = emptyList()
    }

    private fun getSampleData(): List<SpeedResult> {
        return listOf(
            SpeedResult(1, "17 Jun 2026 · 2:34 PM", "Today, 2:34 PM", 94.2, 41.0, 12, 2, "WiFi", "Jio", "Mumbai", "103.24.xx.xx"),
            SpeedResult(2, "17 Jun 2026 · 9:12 AM", "Today, 9:12 AM", 178.5, 28.4, 22, 4, "5G", "Airtel", "Mumbai", "103.24.xx.xx"),
            SpeedResult(3, "16 Jun 2026 · 8:40 PM", "Yesterday, 8:40 PM", 87.4, 32.1, 18, 3, "WiFi", "Jio", "Mumbai", "103.24.xx.xx"),
            SpeedResult(4, "16 Jun 2026 · 1:15 PM", "Yesterday, 1:15 PM", 142.0, 24.7, 19, 5, "5G", "Airtel", "Mumbai", "103.24.xx.xx"),
            SpeedResult(5, "15 Jun 2026 · 7:02 PM", "Mon, 7:02 PM", 96.8, 38.2, 14, 2, "WiFi", "Jio", "Mumbai", "103.24.xx.xx"),
            SpeedResult(6, "15 Jun 2026 · 11:24 AM", "Mon, 11:24 AM", 38.6, 12.1, 42, 8, "4G", "Vi", "Mumbai", "103.24.xx.xx"),
            SpeedResult(7, "14 Jun 2026 · 6:18 PM", "Sun, 6:18 PM", 102.4, 44.0, 0, 0, "WiFi", "Jio", "Mumbai", "103.24.xx.xx")
        )
    }
}

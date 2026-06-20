package com.example.netpulse.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.netpulse.data.SpeedResult
import com.example.netpulse.ui.components.HistoryCard
import com.example.netpulse.ui.theme.DarkColor
import com.example.netpulse.ui.theme.DarkGradient
import com.example.netpulse.ui.theme.Teal200
import com.example.netpulse.ui.viewmodel.HistoryViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val results by viewModel.allResults.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredResults = if (selectedFilter == "All") {
        results
    } else {
        results.filter { it.networkType == selectedFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History", color = Color.White) },
                actions = {
                    IconButton(onClick = { viewModel.clearAll() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkColor)
            )
        },
        containerColor = DarkColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DarkGradient)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "WiFi", "5G", "4G").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Teal200,
                            selectedLabelColor = Color.Black,
                            labelColor = Color.White
                        )
                    )
                }
            }

            if (filteredResults.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No results found", color = Color.White.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredResults, key = { it.id }) { result ->
                        HistoryCard(
                            result = result,
                            dateLabel = formatDateLabel(result.timestamp),
                            onDelete = { viewModel.deleteResult(result.id) },
                            onShare = { /* TODO */ }
                        )
                    }
                }
            }
        }
    }
}

fun formatDateLabel(timestamp: Long): String {
    val now = Calendar.getInstance()
    val resultCal = Calendar.getInstance().apply { timeInMillis = timestamp }

    return when {
        isSameDay(now, resultCal) -> "Today, " + formatTime(timestamp)
        isYesterday(now, resultCal) -> "Yesterday, " + formatTime(timestamp)
        else -> java.text.SimpleDateFormat("EEE, h:mm a", Locale.getDefault()).format(Date(timestamp))
    }
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun isYesterday(now: Calendar, then: Calendar): Boolean {
    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return yesterday.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
}

fun formatTime(timestamp: Long): String {
    return java.text.SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
}

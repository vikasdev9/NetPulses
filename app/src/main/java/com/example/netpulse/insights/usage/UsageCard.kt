package com.example.netpulse.insights.usage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netpulse.data.datastore.UserPreferences
import kotlinx.coroutines.flow.*

class UsageViewModel(private val userPreferences: UserPreferences) : ViewModel() {
    val state: StateFlow<UsageState> = combine(
        userPreferences.usageTodayMb,
        userPreferences.usageWeekMb,
        userPreferences.usageMonthMb,
        userPreferences.dataPlanLimitGb
    ) { today, week, month, limit ->
        UsageState(
            today = formatUsage(today),
            week = formatUsage(week),
            month = formatUsage(month),
            progress = (month / 1024f) / limit.toFloat(),
            limitGb = limit
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UsageState())

    private fun formatUsage(mb: Float): String {
        return if (mb < 1000) {
            "%.0f MB".format(mb)
        } else {
            "%.2f GB".format(mb / 1024f)
        }
    }
}

data class UsageState(
    val today: String = "0 MB",
    val week: String = "0 MB",
    val month: String = "0 MB",
    val progress: Float = 0f,
    val limitGb: Int = 100
)

@Composable
fun UsageCard(viewModel: UsageViewModel) {
    val state by viewModel.state.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Estimated Data Usage",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                UsageMetric("Today", state.today, Modifier.weight(1f))
                UsageMetric("This Week", state.week, Modifier.weight(1f))
                UsageMetric("This Month", state.month, Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Monthly Plan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${state.limitGb} GB Limit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { state.progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun UsageMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

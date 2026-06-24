package com.example.netpulse.insights.wifistability

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*

class WifiStabilityViewModel(private val dao: WifiStabilityDao) : ViewModel() {
    val state: StateFlow<WifiStabilityState> = dao.getAll().map { list ->
        if (list.isEmpty()) return@map WifiStabilityState()
        
        val today = list.filter { it.timestamp >= System.currentTimeMillis() - 86400000 }
        val uptime = if (today.isNotEmpty()) today.count { it.isConnected }.toDouble() / today.size * 100 else 0.0
        val avgRssi = if (today.isNotEmpty()) today.map { it.rssi }.average() else 0.0
        val dropouts = today.zipWithNext().count { it.first.isConnected && !it.second.isConnected }
        
        WifiStabilityState(
            uptimePercent = uptime.toInt(),
            signalLabel = getSignalLabel(avgRssi.toInt()),
            dropoutCount = dropouts,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WifiStabilityState())

    private fun getSignalLabel(rssi: Int): String = when {
        rssi > -50 -> "Excellent"
        rssi > -60 -> "Good"
        rssi > -70 -> "Fair"
        else -> "Weak"
    }
}

data class WifiStabilityState(
    val uptimePercent: Int = 0,
    val signalLabel: String = "Unknown",
    val dropoutCount: Int = 0,
    val isLoading: Boolean = true
)

@Composable
fun WifiStabilityCard(viewModel: WifiStabilityViewModel) {
    val state by viewModel.state.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SignalWifi4Bar, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text("Wi-Fi Stability", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StabilityItem("Uptime", "${state.uptimePercent}%", Modifier.weight(1f))
                StabilityItem("Signal", state.signalLabel, Modifier.weight(1f))
                StabilityItem("Dropouts", state.dropoutCount.toString(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StabilityItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

package com.example.netpulse.thermal.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.thermal.components.ThermalGauge
import com.example.netpulse.thermal.components.ThermalHistoryChart
import com.example.netpulse.thermal.viewmodel.ThermalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThermalDetailsScreen(
    onBack: () -> Unit,
    viewModel: ThermalViewModel
) {
    val currentData by viewModel.currentThermalData.collectAsState()
    val history by viewModel.history.collectAsState()
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thermal Monitor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, null)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ThermalGauge(
                        temperature = currentData.temperature,
                        statusColor = currentData.status.color
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Current Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        InfoRow("Thermal API", currentData.thermalApiStatus)
                        InfoRow("Battery Level", "${currentData.batteryLevel}%")
                        InfoRow("Power Status", if (currentData.isCharging) "Charging" else "Discharging")
                        InfoRow("Last Updated", "Just now")
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Temperature History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        ThermalHistoryChart(history = history)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionBtn(
                        text = "Refresh",
                        icon = Icons.Default.Refresh,
                        onClick = { viewModel.refreshData() },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionBtn(
                        text = "Disable",
                        icon = Icons.Default.Block,
                        onClick = { viewModel.toggleMonitoring(false) },
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            item {
                Text(
                    "Monitoring Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        ListItem(
                            headlineContent = { Text("Enabled") },
                            trailingContent = {
                                Switch(
                                    checked = settings.enabled,
                                    onCheckedChange = { viewModel.toggleMonitoring(it) }
                                )
                            }
                        )
                        ListItem(
                            headlineContent = { Text("Background Monitoring") },
                            supportingContent = { Text("Uses WorkManager for periodic checks") },
                            trailingContent = {
                                Switch(
                                    checked = settings.backgroundMonitoring,
                                    onCheckedChange = { /* Update pref */ }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionBtn(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.1f), contentColor = color)
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

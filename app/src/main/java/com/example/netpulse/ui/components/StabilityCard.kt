package com.example.netpulse.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.ui.theme.NetPulseTheme
import com.example.netpulse.ui.viewmodel.StabilityMetrics

@Composable
fun StabilityCard(
    metrics: StabilityMetrics,
    modifier: Modifier = Modifier
) {
    AnalyticsCard(title = "Wi-Fi Stability", icon = Icons.Default.SignalWifi4Bar, modifier = modifier) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Uptime today", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    Text("${metrics.uptimePercentage}%", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("Signal Strength", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    Text(metrics.signalLabel, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text("Live Signal Strength", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (metrics.liveSignalStrength / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                StabilityInfoItem("Disconnections", metrics.disconnectionCount.toString(), Modifier.weight(1f))
                StabilityInfoItem("Ping Stability", "${"%.1f".format(metrics.pingStability)} ms", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StabilityInfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        Text(value, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview
@Composable
fun StabilityCardPreview() {
    NetPulseTheme {
        StabilityCard(StabilityMetrics(98, "Excellent", 2, 4.5f, 85))
    }
}

package com.example.netpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.ui.theme.NetPulseTheme
import com.example.netpulse.ui.viewmodel.IspPerformance

@Composable
fun IspPerformanceCard(
    performance: IspPerformance,
    modifier: Modifier = Modifier
) {
    AnalyticsCard(title = "ISP Performance", icon = Icons.Default.Router, modifier = modifier) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ISP", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Current Provider", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    Text(performance.rankBadge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text("Advertised vs Actual", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            ComparisonBar("Actual Avg", performance.actualAvg, performance.advertised, MaterialTheme.colorScheme.primary)
            ComparisonBar("Advertised", performance.advertised, performance.advertised, MaterialTheme.colorScheme.outline)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                ScoreItem("Delivery", "${performance.deliveryScore}%", Modifier.weight(1f))
                ScoreItem("Reliability", "${performance.reliabilityScore}%", Modifier.weight(1f))
                ScoreItem("Industry Avg", "78%", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ComparisonBar(label: String, value: Float, max: Float, color: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${value.toInt()} Mbps", fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (value / max).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun ScoreItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        Text(value, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview
@Composable
fun IspPerformanceCardPreview() {
    NetPulseTheme {
        IspPerformanceCard(IspPerformance(85, 92, "Top 10%", 85f, 100f))
    }
}

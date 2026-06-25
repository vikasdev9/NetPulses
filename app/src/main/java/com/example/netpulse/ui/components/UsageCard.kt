package com.example.netpulse.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataUsage
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
import com.example.netpulse.ui.viewmodel.EstimatedUsage

@Composable
fun UsageCard(
    usage: EstimatedUsage,
    modifier: Modifier = Modifier
) {
    AnalyticsCard(title = "Estimated Test Usage", icon = Icons.Default.DataUsage, modifier = modifier) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                UsageMetric("Today", formatUsage(usage.todayMB))
                UsageMetric("Week", formatUsage(usage.weekMB))
                UsageMetric("Month", formatUsage(usage.monthMB))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val progress = (usage.monthMB / (usage.planLimitGB * 1024)).coerceIn(0f, 1f)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Monthly Plan Progress", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                Text("${(progress * 100).toInt()}%", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${formatUsage(usage.monthMB)} of ${usage.planLimitGB.toInt()} GB used",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun UsageMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        Text(value, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

private fun formatUsage(mb: Float): String {
    return if (mb < 1000) {
        "${mb.toInt()} MB"
    } else {
        "%.2f GB".format(mb / 1024f)
    }
}

@Preview
@Composable
fun UsageCardPreview() {
    NetPulseTheme {
        UsageCard(usage = EstimatedUsage(120f, 850f, 1200f, 100f))
    }
}

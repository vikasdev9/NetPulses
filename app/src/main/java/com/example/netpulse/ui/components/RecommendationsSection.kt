package com.example.netpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import com.example.netpulse.ui.viewmodel.RecommendationItem
import com.example.netpulse.ui.viewmodel.RecommendationPriority

@Composable
fun RecommendationsSection(
    recommendations: List<RecommendationItem>,
    onDismiss: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Smart Recommendations",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        if (recommendations.isEmpty() || recommendations.all { it.isDismissed }) {
            AnalyticsCard(title = "All Clear", icon = Icons.Default.CheckCircle) {
                Text("No issues detected. Your connection is performing optimally.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
        } else {
            recommendations.filter { !it.isDismissed }.forEach { item ->
                RecommendationCard(item, onDismiss)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun RecommendationCard(
    item: RecommendationItem,
    onDismiss: (String) -> Unit
) {
    val color = when (item.priority) {
        RecommendationPriority.HIGH -> Color.Red
        RecommendationPriority.MEDIUM -> Color(0xFFFFBF00)
        RecommendationPriority.LOW -> Color(0xFF3B8BFF)
    }
    
    val icon = when (item.priority) {
        RecommendationPriority.HIGH -> Icons.Default.Error
        RecommendationPriority.MEDIUM -> Icons.Default.Warning
        RecommendationPriority.LOW -> Icons.Default.Info
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(4.dp))
                Text(item.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { onDismiss(item.id) }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Preview
@Composable
fun RecommendationsSectionPreview() {
    NetPulseTheme {
        RecommendationsSection(
            recommendations = listOf(
                RecommendationItem("1", "High latency", "Try connecting via ethernet", RecommendationPriority.HIGH),
                RecommendationItem("2", "Weak signal", "Move closer to router", RecommendationPriority.MEDIUM)
            ),
            onDismiss = {}
        )
    }
}

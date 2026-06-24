package com.example.netpulse.insights.recommendations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RecommendationsList(viewModel: RecommendationsViewModel) {
    val recommendations by viewModel.recommendations.collectAsState()

    if (recommendations.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
            Text("✅ All clear! Your network is performing well.", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            recommendations.forEach { rec ->
                RecommendationItem(rec)
            }
        }
    }
}

@Composable
fun RecommendationItem(recommendation: Recommendation) {
    val color = when(recommendation.priority) {
        Priority.HIGH -> Color(0xFFF44336)
        Priority.MEDIUM -> Color(0xFFFFB300)
        Priority.LOW -> Color(0xFF2196F3)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = when(recommendation.priority) {
                    Priority.HIGH -> Icons.Default.Error
                    Priority.MEDIUM -> Icons.Default.Warning
                    Priority.LOW -> Icons.Default.Info
                },
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(recommendation.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
                Text(recommendation.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            IconButton(onClick = { /* Dismiss logic */ }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

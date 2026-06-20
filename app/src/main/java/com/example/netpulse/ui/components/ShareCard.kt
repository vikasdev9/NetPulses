package com.example.netpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.data.SpeedResult
import com.example.netpulse.ui.theme.DarkColor2
import com.example.netpulse.ui.theme.Teal200
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ShareCard(result: SpeedResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkColor2),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "NetPulse Speed Test",
                style = MaterialTheme.typography.titleMedium,
                color = Teal200
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem("DOWNLOAD", "%.1f".format(result.downloadMbps), "Mbps")
                MetricItem("UPLOAD", "%.1f".format(result.uploadMbps), "Mbps")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "${result.pingMs} ms Ping",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val sdf = SimpleDateFormat("dd MMM yyyy · h:mm a", Locale.getDefault())
            Text(
                text = sdf.format(Date(result.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${result.networkType} · ${result.isp} · ${result.location}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
        Text(value, fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text(unit, style = MaterialTheme.typography.labelSmall, color = Teal200)
    }
}

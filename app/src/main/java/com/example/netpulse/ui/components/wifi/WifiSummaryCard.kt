package com.example.netpulse.ui.components.wifi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.data.wifi.WifiNetwork

@Composable
fun WifiSummaryCard(
    networks: List<WifiNetwork>
) {
    val total = networks.size
    val bestNetwork = networks.maxByOrNull { it.signalStrength }
    val openCount = networks.count { it.securityType == "Open" }
    val band5GCount = networks.count { it.frequencyBand == "5.0 GHz" }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            SummaryItem(
                icon = Icons.Default.Wifi,
                value = total.toString(),
                label = "Total Networks",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            SummaryItem(
                icon = Icons.Default.SignalWifi4Bar,
                value = if (bestNetwork != null) "${bestNetwork.signalStrength} dBm" else "--",
                label = "Best Signal",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            SummaryItem(
                icon = Icons.Default.LockOpen,
                value = openCount.toString(),
                label = "Open Networks",
                modifier = Modifier.weight(1f),
                valueColor = if (openCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            SummaryItem(
                icon = Icons.Default.WifiTethering,
                value = band5GCount.toString(),
                label = "5GHz Networks",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SummaryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}

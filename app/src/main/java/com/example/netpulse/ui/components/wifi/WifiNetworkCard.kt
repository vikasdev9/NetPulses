package com.example.netpulse.ui.components.wifi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.data.wifi.WifiNetwork

@Composable
fun WifiNetworkCard(
    network: WifiNetwork,
    onFavoriteClick: (WifiNetwork) -> Unit = {},
    onClick: (WifiNetwork) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    val signalColor = when {
        network.signalStrength > -50 -> Color(0xFF00E676) // Excellent
        network.signalStrength > -60 -> Color(0xFF3B8BFF) // Good
        network.signalStrength > -70 -> Color(0xFFFFB300) // Fair
        else -> Color(0xFFEF4444) // Weak
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(0.5.dp, Color(0xFF1E2740), RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131929))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Signal Icon (4-bar wifi style placeholder)
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = signalColor,
                    modifier = Modifier.size(28.dp)
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = if (network.isHidden) "Hidden Network" else network.ssid,
                        color = if (network.isHidden) Color(0xFF8892A4) else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = if (network.isHidden) FontStyle.Italic else FontStyle.Normal
                    )
                    Text(
                        text = network.bssid,
                        color = Color(0xFF8892A4),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row {
                        WifiChip(
                            text = network.frequencyBand,
                            color = when(network.frequencyBand) {
                                "2.4 GHz" -> Color(0xFFFFB300)
                                "5.0 GHz" -> Color(0xFF3B8BFF)
                                else -> Color(0xFF00E676)
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        WifiChip(
                            text = network.securityType,
                            color = when(network.securityType) {
                                "Open" -> Color(0xFFEF4444)
                                "WPA2" -> Color(0xFF00E676)
                                "WPA3" -> Color(0xFF00D4FF)
                                else -> Color(0xFFFFB300)
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        WifiChip(text = "Ch ${network.channel}", color = Color(0xFF8892A4))
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Signal", color = Color(0xFF8892A4), fontSize = 10.sp)
                            Text("${network.signalStrength} dBm", color = Color(0xFF8892A4), fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF1E2740))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(network.signalPercentage)
                                    .background(signalColor)
                            )
                        }
                    }
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF8892A4),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = Color(0xFF1E2740), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailRow("Channel Width", network.channelWidth)
                    DetailRow("Standard", network.standard)
                    DetailRow("Timestamp", "Found ${formatTimestamp(network.timestamp)}")
                }
            }
        }
    }
}

@Composable
fun WifiChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(text = text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF8892A4), fontSize = 12.sp)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000 -> "${diff / 1000}s ago"
        else -> "${diff / 60000}m ago"
    }
}

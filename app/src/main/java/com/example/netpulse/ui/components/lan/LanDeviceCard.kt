package com.example.netpulse.ui.components.lan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.data.lan.DeviceType
import com.example.netpulse.data.lan.LanDevice

@Composable
fun LanDeviceCard(
    device: LanDevice,
    onFavoriteToggle: () -> Unit,
    onRename: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (device.isCurrentDevice) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
                else MaterialTheme.colorScheme.surface
        ),
        border = if (device.isCurrentDevice) 
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) 
            else androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Device Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (device.isOnline) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getDeviceIcon(device),
                        contentDescription = null,
                        tint = if (device.isOnline) MaterialTheme.colorScheme.primary 
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.nickname ?: device.hostname,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = device.ipAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                // Badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (device.isRouter) {
                        Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                            Text("ROUTER", fontSize = 9.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (device.isFavorite) {
                        Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp), tint = Color(0xFFFFB300))
                    }
                }
            }
            
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    DetailRow("Hostname", device.hostname)
                    DetailRow("MAC Address", device.macAddress)
                    DetailRow("Vendor", device.vendor)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onRename) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rename")
                        }
                        TextButton(onClick = onFavoriteToggle) {
                            Icon(
                                imageVector = if (device.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (device.isFavorite) "Unfavorite" else "Favorite")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

private fun getDeviceIcon(device: LanDevice): ImageVector {
    return when (device.deviceType) {
        DeviceType.ROUTER -> Icons.Default.Router
        DeviceType.PHONE -> Icons.Default.Smartphone
        DeviceType.TV -> Icons.Default.Tv
        DeviceType.LAPTOP -> Icons.Default.Laptop
        DeviceType.DESKTOP -> Icons.Default.DesktopWindows
        DeviceType.PRINTER -> Icons.Default.Print
        DeviceType.IOT -> Icons.Default.Sensors
        DeviceType.CAMERA -> Icons.Default.Videocam
        else -> if (device.isRouter) Icons.Default.Router else Icons.Default.Devices
    }
}

package com.example.netpulse.ui.components.lan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Note
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LanDeviceCard(
    device: LanDevice,
    onFavoriteToggle: () -> Unit,
    onRename: () -> Unit,
    onViewHistory: () -> Unit,
    onAddNote: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (device.isCurrentDevice) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) 
                else MaterialTheme.colorScheme.surface
        ),
        border = if (device.isCurrentDevice) 
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) 
            else androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Device Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = device.nickname ?: device.hostname,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (device.isFavorite) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Star, null, modifier = Modifier.size(14.dp), tint = Color(0xFFFFB300))
                        }
                    }
                    Text(
                        text = device.ipAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                // Latency & Status
                Column(horizontalAlignment = Alignment.End) {
                    if (device.isOnline) {
                        Text(
                            text = if (device.latencyMs >= 0) "${device.latencyMs} ms" else "—",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (device.latencyMs in 0..50) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                        )
                    } else {
                        Text(
                            text = "Offline",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Badges
            if (device.isRouter || device.isCurrentDevice || device.isUnknown) {
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    if (device.isRouter) {
                        BadgeItem("ROUTER", MaterialTheme.colorScheme.secondary)
                    }
                    if (device.isCurrentDevice) {
                        BadgeItem("THIS DEVICE", MaterialTheme.colorScheme.primary)
                    }
                    if (device.isUnknown) {
                        BadgeItem("UNKNOWN", MaterialTheme.colorScheme.error)
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    DetailRow("Hostname", device.hostname)
                    DetailRow("MAC Address", device.macAddress)
                    DetailRow("Vendor", device.vendor)
                    DetailRow("Device Type", device.deviceType.label)
                    DetailRow("First Seen", SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(device.firstSeen)))
                    DetailRow("Last Seen", SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(device.lastSeen)))
                    
                    if (!device.notes.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Outlined.Note, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(device.notes, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            IconButton(onClick = onRename) { Icon(Icons.Default.Edit, "Rename", modifier = Modifier.size(20.dp)) }
                            IconButton(onClick = onAddNote) { Icon(Icons.Outlined.Note, "Notes", modifier = Modifier.size(20.dp)) }
                            IconButton(onClick = onViewHistory) { Icon(Icons.Outlined.History, "History", modifier = Modifier.size(20.dp)) }
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("IP Address", device.ipAddress)
                                clipboard.setPrimaryClip(clip)
                                android.widget.Toast.makeText(context, "IP Copied", android.widget.Toast.LENGTH_SHORT).show()
                            }) { Icon(Icons.Default.ContentCopy, "Copy IP", modifier = Modifier.size(20.dp)) }
                            
                            if (device.isRouter) {
                                IconButton(onClick = {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("http://${device.ipAddress}"))
                                    context.startActivity(intent)
                                }) { Icon(Icons.Default.OpenInBrowser, "Open Router", modifier = Modifier.size(20.dp)) }
                            }
                            
                            IconButton(onClick = {
                                val shareText = """
                                    Device: ${device.nickname ?: device.hostname}
                                    IP: ${device.ipAddress}
                                    MAC: ${device.macAddress}
                                    Vendor: ${device.vendor}
                                    Status: ${if(device.isOnline) "Online" else "Offline"}
                                    Last Seen: ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(device.lastSeen))}
                                """.trimIndent()
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Share Device Info"))
                            }) { Icon(Icons.Default.Share, "Share", modifier = Modifier.size(20.dp)) }
                        }
                        
                        Button(
                            onClick = onFavoriteToggle,
                            shape = RoundedCornerShape(12.dp),
                            colors = if (device.isFavorite) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                                    else ButtonDefaults.buttonColors()
                        ) {
                            Icon(
                                imageVector = if (device.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (device.isFavorite) "Remove Star" else "Favorite", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeItem(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
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
        DeviceType.NAS -> Icons.Default.Storage
        DeviceType.CONSOLE -> Icons.Default.SportsEsports
        else -> if (device.isRouter) Icons.Default.Router else Icons.Default.Devices
    }
}

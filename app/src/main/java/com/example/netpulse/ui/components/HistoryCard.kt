package com.example.netpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.example.netpulse.data.SpeedResult

@Composable
fun HistoryCard(
    result: SpeedResult,
    dateLabel: String,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val networkColor = when (result.networkType) {
        "WiFi" -> MaterialTheme.colorScheme.primary
        "5G" -> MaterialTheme.colorScheme.tertiary
        "4G" -> Color(0xFFFFB300) // Amber
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val badgeBg = when (result.networkType) {
        "WiFi" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        "5G" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        "4G" -> Color(0xFFFFB300).copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .clickable { onShare() }
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .background(networkColor)
        )

        Row(
            modifier = Modifier
                .padding(14.dp)
                .padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateLabel,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                HistoryBadge(
                    text = result.networkType,
                    textColor = networkColor,
                    bgColor = badgeBg
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("↓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "%.1f Mbps".format(result.downloadMbps),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("↑", color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "%.1f Mbps".format(result.uploadMbps),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = "${result.pingMs} ms ping",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun HistoryBadge(
    text: String,
    textColor: Color,
    bgColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

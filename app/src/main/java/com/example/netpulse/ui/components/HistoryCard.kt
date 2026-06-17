package com.example.netpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
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
import com.example.netpulse.ui.theme.*

@Composable
fun HistoryCard(
    result: SpeedResult,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val networkColor = when (result.networkType) {
        "WiFi" -> Color(0xFF3B8BFF)
        "5G" -> Color(0xFF00E676)
        "4G" -> Color(0xFFFFB300)
        else -> TextSecondary
    }

    val badgeBg = when (result.networkType) {
        "WiFi" -> Color(0xFF0D1E2E)
        "5G" -> Color(0xFF0D2E1A)
        "4G" -> Color(0xFF2E1F0D)
        else -> CardSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardSurface)
            .border(0.5.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable { onShare() }
    ) {
        // Left colored border (4dp wide)
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
                .padding(start = 4.dp), // Space for the colored border
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: Date and Badge
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.dateLabel,
                    color = Color.White,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                NetworkBadge(
                    text = result.networkType,
                    textColor = networkColor,
                    bgColor = badgeBg
                )
            }

            // Right Column: Metrics
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("↓", color = Color(0xFF3B8BFF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${result.downloadMbps} Mbps",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = MonospaceFontFamily
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("↑", color = Color(0xFF00D4FF), fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${result.uploadMbps} Mbps",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontFamily = MonospaceFontFamily
                    )
                }
                Text(
                    text = "${result.pingMs} ms ping",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Chevron
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun NetworkBadge(
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

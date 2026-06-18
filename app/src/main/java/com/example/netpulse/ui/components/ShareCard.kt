package com.example.netpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.data.SpeedResult
import com.example.netpulse.ui.theme.*

@Composable
fun ShareCard(
    result: SpeedResult,
    isPro: Boolean,
    modifier: Modifier = Modifier
) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF3B8BFF), Color(0xFF00D4FF))
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .drawBehind {
                drawRoundRect(
                    brush = gradientBrush,
                    style = Stroke(width = 1.5.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface)
            .padding(20.dp)
    ) {
        Column {
            // ROW 1 — APP HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF3B8BFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NetPulse",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "REPORT",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ROW 2 — DOWNLOAD SECTION
            Column {
                Text(
                    text = "DOWNLOAD",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%.1f".format(result.downloadMbps),
                        color = Color.White,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MonospaceFontFamily
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mbps",
                        color = TextSecondary,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ROW 3 — UPLOAD + PING SIDE BY SIDE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricSubCard(
                    label = "UPLOAD",
                    value = "%.1f".format(result.uploadMbps),
                    icon = "↑",
                    iconColor = Color(0xFF00D4FF),
                    modifier = Modifier.weight(1f)
                )
                MetricSubCard(
                    label = "PING",
                    value = "${result.pingMs} ms",
                    icon = "∿",
                    iconColor = Color(0xFF00E676),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ROW 4 — NETWORK INFO ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${result.networkType} · ${result.isp} · ${result.location}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = result.timestamp,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            // ROW 5 — WATERMARK
            if (!isPro) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "NETPULSE.APP",
                        color = Color(0xFF2E3A50),
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun MetricSubCard(
    label: String,
    value: String,
    icon: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0D1320))
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = icon,
                    color = iconColor,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    color = TextSecondary,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MonospaceFontFamily
            )
        }
    }
}

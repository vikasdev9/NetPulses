package com.example.netpulse.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.data.analytics.DailyUsage
import com.example.netpulse.ui.theme.TextSecondary

@Composable
fun DataBarChart(
    weeklyUsage: List<DailyUsage>,
    modifier: Modifier = Modifier
) {
    val mobileColor = Color(0xFF3B8BFF)
    val wifiColor = Color(0xFF00D4FF).copy(alpha = 0.6f)
    
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(weeklyUsage) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        )
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = size.width / (weeklyUsage.size * 2f)
                val spaceWidth = barWidth
                val maxUsage = weeklyUsage.maxOfOrNull { it.totalAll } ?: 1L
                val maxPx = size.height

                weeklyUsage.forEachIndexed { index, daily ->
                    val x = spaceWidth / 2f + index * (barWidth + spaceWidth)
                    
                    val mobileHeight = (daily.totalMobile.toFloat() / maxUsage) * maxPx * animationProgress.value
                    val wifiHeight = (daily.totalWifi.toFloat() / maxUsage) * maxPx * animationProgress.value
                    
                    // Mobile part
                    drawRoundRect(
                        color = mobileColor,
                        topLeft = Offset(x, maxPx - mobileHeight),
                        size = Size(barWidth, mobileHeight),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                    
                    // WiFi part (stacked)
                    drawRoundRect(
                        color = wifiColor,
                        topLeft = Offset(x, maxPx - mobileHeight - wifiHeight),
                        size = Size(barWidth, wifiHeight),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weeklyUsage.forEach { daily ->
                Text(
                    text = daily.dayLabel.take(3),
                    color = TextSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.width(32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = mobileColor, label = "Mobile")
            Spacer(modifier = Modifier.width(24.dp))
            LegendItem(color = wifiColor, label = "WiFi")
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp
        )
    }
}

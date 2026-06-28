package com.example.netpulse.thermal.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ThermalGauge(
    temperature: Float,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedTemp by animateFloatAsState(
        targetValue = temperature,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "temp"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val strokeWidth = 12.dp.toPx()
            val innerStrokeWidth = 4.dp.toPx()
            
            // Background arc
            drawArc(
                color = Color.LightGray.copy(alpha = 0.2f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc
            val sweepAngle = (animatedTemp / 60f).coerceIn(0f, 1f) * 270f
            drawArc(
                brush = Brush.sweepGradient(
                    0f to statusColor.copy(alpha = 0.5f),
                    0.5f to statusColor,
                    1f to statusColor
                ),
                startAngle = 135f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            
            // Gauge ticks
            for (i in 0..12) {
                val angle = 135f + (i * 22.5f)
                // Draw small ticks...
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${"%.1f".format(animatedTemp)}°C",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Device Temperature",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

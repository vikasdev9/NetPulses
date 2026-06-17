package com.example.netpulse.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedGauge(
    speedMbps: Float,
    maxSpeed: Float = 200f,
    statusLabel: String = "READY",
    modifier: Modifier = Modifier
) {
    val animatedSpeed by animateFloatAsState(
        targetValue = speedMbps,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "gaugeSpeed"
    )

    Box(
        modifier = modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2 - 20.dp.toPx()
            val startAngle = 150f
            val sweepAngle = 240f
            val strokeWidth = 12.dp.toPx()

            // Track
            drawArc(
                color = GaugeTrack,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius)
            )

            // Fill
            val fillSweep = (animatedSpeed / maxSpeed).coerceIn(0f, 1f) * sweepAngle
            val gradient = Brush.sweepGradient(
                0.0f to GaugeCyan,
                0.6f to GaugeBlue,
                center = center
            )
            
            // We use a simpler gradient for the arc to match spec
            val linearGradient = Brush.linearGradient(
                colors = listOf(GaugeCyan, GaugeBlue),
                start = Offset(center.x - radius, center.y + radius),
                end = Offset(center.x + radius, center.y - radius)
            )

            drawArc(
                brush = linearGradient,
                startAngle = startAngle,
                sweepAngle = fillSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius)
            )

            // Glow effect (Simplified with multiple arcs)
            drawArc(
                brush = linearGradient,
                startAngle = startAngle,
                sweepAngle = fillSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth + 4.dp.toPx(), cap = StrokeCap.Round),
                alpha = 0.2f,
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius)
            )

            // Dot at the tip
            if (fillSweep > 0) {
                val endAngleRad = Math.toRadians((startAngle + fillSweep).toDouble())
                val dotX = center.x + radius * cos(endAngleRad).toFloat()
                val dotY = center.y + radius * sin(endAngleRad).toFloat()
                
                // Glow for dot
                drawCircle(
                    color = GaugeBlue.copy(alpha = 0.5f),
                    radius = 8.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }

            // Ticks and Labels
            val tickSteps = listOf(0, 50, 100, 150, 200)
            tickSteps.forEach { tick ->
                val tickAngle = startAngle + (tick.toFloat() / maxSpeed) * sweepAngle
                val angleRad = Math.toRadians(tickAngle.toDouble())
                
                val innerX = center.x + (radius + 10.dp.toPx()) * cos(angleRad).toFloat()
                val innerY = center.y + (radius + 10.dp.toPx()) * sin(angleRad).toFloat()
                val outerX = center.x + (radius + 18.dp.toPx()) * cos(angleRad).toFloat()
                val outerY = center.y + (radius + 18.dp.toPx()) * sin(angleRad).toFloat()

                drawLine(
                    color = TextSecondary.copy(alpha = 0.5f),
                    start = Offset(innerX, innerY),
                    end = Offset(outerX, outerY),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%.1f", animatedSpeed),
                style = SpeedTypography,
                color = TextPrimary
            )
            Text(
                text = "Mbps",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusLabel,
                color = StatusCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Tick Labels
        val tickSteps = listOf(0, 50, 100, 150, 200)
        tickSteps.forEach { tick ->
            val startAngle = 150f
            val sweepAngle = 240f
            val maxSpeed = 200f
            val tickAngle = startAngle + (tick.toFloat() / maxSpeed) * sweepAngle
            val angleRad = Math.toRadians(tickAngle.toDouble())
            
            // Adjust distance for labels
            val labelRadius = 120.dp
            
            val offsetX = (labelRadius.value * cos(angleRad)).toFloat()
            val offsetY = (labelRadius.value * sin(angleRad)).toFloat()

            Text(
                text = tick.toString(),
                color = TextSecondary,
                fontSize = 10.sp,
                modifier = Modifier
                    .offset(x = offsetX.dp, y = offsetY.dp)
            )
        }
    }
}

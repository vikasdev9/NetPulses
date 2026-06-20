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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

private data class GaugeTick(val value: Float, val label: String)

/**
 * A professional Speedometer Gauge for NetPulse.
 * 
 * FIXES APPLIED:
 * 1. Visual center pushed down (h * 0.52f) to prevent text/arc overlap.
 * 2. Scale fixed to 0 -> 100 Mbps.
 * 3. Text moved to Box overlay with Y-offset for perfect centering in the "mouth" of the arc.
 */
@Composable
fun SpeedGauge(
    speedMbps: Float,
    maxSpeed: Float = 100f,
    statusLabel: String,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    // Smooth transition for the speed needle/arc
    val animatedSpeed by animateFloatAsState(
        targetValue = speedMbps,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "SpeedAnimation"
    )

    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        // LAYER 1: The Gauge Arc and Ticks
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            
            // KEY FIX 1: cy is NOT h/2 — push center DOWN by 15%
            // so the visual center sits in the mouth of the gauge
            val cy = h * 0.52f  
            
            val radius = w * 0.38f
            val strokeWidth = w * 0.045f

            // Geometry: 135° start (bottom-left), 270° sweep (around to bottom-right)
            val startAngle = 135f
            val sweepAngle = 270f
            val ratio = (animatedSpeed / maxSpeed).coerceIn(0f, 1f)

            // 1. Draw Background Track
            drawArc(
                color = Color(0xFF1E2D47),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(cx - radius, cy - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Draw Active Speed Gradient Arc
            if (ratio > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF00D4FF),
                            Color(0xFF3B8BFF)
                        ),
                        center = Offset(cx, cy)
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * ratio,
                    useCenter = false,
                    topLeft = Offset(cx - radius, cy - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // 3. Glowing tip dot at the end of the current speed
                val tipAngle = Math.toRadians((startAngle + sweepAngle * ratio).toDouble())
                val tipX = cx + radius * cos(tipAngle).toFloat()
                val tipY = cy + radius * sin(tipAngle).toFloat()
                drawCircle(
                    color = Color.White,
                    radius = strokeWidth * 0.45f,
                    center = Offset(tipX, tipY)
                )
            }

            // 4. Tick Marks and Scale Labels (0, 25, 50, 75, 100)
            val ticks = listOf(
                GaugeTick(0f, "0"),
                GaugeTick(25f, "25"),
                GaugeTick(50f, "50"),
                GaugeTick(75f, "75"),
                GaugeTick(100f, "100")
            )

            ticks.forEach { tick ->
                val tickRatio = tick.value / maxSpeed
                val tickAngle = Math.toRadians((startAngle + sweepAngle * tickRatio).toDouble())
                
                // Draw tick line
                val outerR = radius + strokeWidth * 0.3f
                val innerR = radius - strokeWidth * 0.3f
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(
                        cx + innerR * cos(tickAngle).toFloat(),
                        cy + innerR * sin(tickAngle).toFloat()
                    ),
                    end = Offset(
                        cx + outerR * cos(tickAngle).toFloat(),
                        cy + outerR * sin(tickAngle).toFloat()
                    ),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Draw Scale Label
                val labelR = radius - strokeWidth * 1.8f
                val labelX = cx + labelR * cos(tickAngle).toFloat()
                val labelY = cy + labelR * sin(tickAngle).toFloat()

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#475569")
                        textSize = (w * 0.035f) // Scale text size with gauge
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.MONOSPACE
                    }
                    drawText(
                        tick.label,
                        labelX,
                        labelY + paint.textSize / 3f,
                        paint
                    )
                }
            }
        }

        // LAYER 2: Text Overlay (Centered in the Gauge Mouth)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .offset(y = 24.dp), // KEY FIX 1: Push text DOWN into the open area
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Speed number (Monospace prevents shifting)
            Text(
                text = String.format(Locale.US, "%.1f", speedMbps),
                style = TextStyle(
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White,
                    letterSpacing = (-1).sp
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Unit Label
            Text(
                text = "Mbps",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            )

            Spacer(modifier = Modifier.height(8.dp)) // Premium spacing for status

            // Current Test Status
            Text(
                text = statusLabel.uppercase(),
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp,
                    color = statusColor
                )
            )
        }
    }
}

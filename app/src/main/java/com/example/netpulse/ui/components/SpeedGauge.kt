package com.example.netpulse.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@Composable
fun SpeedGauge(
    speedMbps: Float,          // current live speed
    maxSpeed: Float = 100f,    // max scale value
    statusLabel: String,       // "READY" / "DOWNLOADING..." etc
    statusColor: Color,        // cyan / blue / green
    isRunning: Boolean,        // true = pulse animation on
    modifier: Modifier = Modifier
) {
    // Animate the DISPLAYED number separately from arc:
    val animatedDisplayNum by animateFloatAsState(
        targetValue = speedMbps,
        animationSpec = tween(
            durationMillis = 250,
            easing = LinearEasing  // linear for number rolling
        ),
        label = "DisplayNumberAnimation"
    )

    // Arc animates with ease-out (feels like real acceleration):
    val animatedArcProgress by animateFloatAsState(
        targetValue = (speedMbps / maxSpeed).coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "ArcAnimation"
    )

    // Pulse animation on tip dot:
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseScale by if (isRunning) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "PulseAnimation"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Box(modifier = modifier.aspectRatio(1f)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h * 0.50f
            val radius = w * 0.38f
            val strokeWidth = w * 0.048f
            
            val startAngle = 135f
            val sweepAngle = 270f
            
            // BUG 3 FIX — Define arcRect for all layers
            val arcRectTopLeft = Offset(cx - radius, cy - radius)
            val arcRectSize = Size(radius * 2, radius * 2)

            // 1. Background arc track (#1A2744)
            drawArc(
                color = Color(0xFF1A2744),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = arcRectTopLeft,
                size = arcRectSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Inner shadow arc (#0F1729, 60% alpha, 2px inside)
            val innerShadowRadius = radius - 2f
            drawArc(
                color = Color(0xFF0F1729).copy(alpha = 0.6f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(cx - innerShadowRadius, cy - innerShadowRadius),
                size = Size(innerShadowRadius * 2, innerShadowRadius * 2),
                style = Stroke(width = 2f, cap = StrokeCap.Round)
            )

            if (animatedArcProgress > 0f) {
                // BUG 3 FIX — Use arcRect for glow layers and fill
                
                // 3. Outer glow arc (blue 15% alpha, wide)
                drawArc(
                    color = Color(0xFF3B8BFF).copy(alpha = 0.15f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * animatedArcProgress,
                    useCenter = false,
                    topLeft = arcRectTopLeft,
                    size = arcRectSize,
                    style = Stroke(width = strokeWidth * 2.2f, cap = StrokeCap.Round)
                )

                // 4. Mid glow arc (blue 35% alpha, medium)
                drawArc(
                    color = Color(0xFF3B8BFF).copy(alpha = 0.35f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * animatedArcProgress,
                    useCenter = false,
                    topLeft = arcRectTopLeft,
                    size = arcRectSize,
                    style = Stroke(width = strokeWidth * 1.5f, cap = StrokeCap.Round)
                )

                // 5. Cyan arc (first half of fill)
                drawArc(
                    color = Color(0xFF00D4FF),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * animatedArcProgress * 0.5f,
                    useCenter = false,
                    topLeft = arcRectTopLeft,
                    size = arcRectSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // 6. Blue arc (second half of fill)
                if (animatedArcProgress > 0.5f) {
                    drawArc(
                        color = Color(0xFF3B8BFF),
                        startAngle = startAngle + sweepAngle * animatedArcProgress * 0.5f,
                        sweepAngle = sweepAngle * animatedArcProgress * 0.5f,
                        useCenter = false,
                        topLeft = arcRectTopLeft,
                        size = arcRectSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // BUG 1 FIX — Tip Dot Calculation
                val tipAngle = Math.toRadians((startAngle + sweepAngle * animatedArcProgress).toDouble())
                val tipX = cx + radius * cos(tipAngle).toFloat()
                val tipY = cy + radius * sin(tipAngle).toFloat()
                val tipOffset = Offset(tipX, tipY)

                // 7. Tip outer glow circle (pulsing when running)
                drawCircle(
                    color = Color(0xFF3B8BFF).copy(alpha = 0.25f),
                    radius = strokeWidth * 1.1f * pulseScale,
                    center = tipOffset
                )

                // 8. Tip mid glow circle
                drawCircle(
                    color = Color(0xFF00D4FF).copy(alpha = 0.5f),
                    radius = strokeWidth * 0.7f,
                    center = tipOffset
                )

                // 9. Tip core white dot
                drawCircle(
                    color = Color.White,
                    radius = strokeWidth * 0.42f,
                    center = tipOffset
                )
            }

            // 10. Minor tick marks & 11. Major tick marks
            val majorTicks = listOf(0f, 25f, 50f, 75f, 100f)
            val minorTicks = listOf(12.5f, 37.5f, 62.5f, 87.5f)

            minorTicks.forEach { tick ->
                val tickAngle = Math.toRadians((startAngle + sweepAngle * (tick / maxSpeed)).toDouble())
                val outerR = radius + strokeWidth * 0.45f
                val innerR = outerR - (strokeWidth * 0.3f)
                drawLine(
                    color = Color(0xFF334155).copy(alpha = 0.8f),
                    start = Offset(cx + innerR * cos(tickAngle).toFloat(), cy + innerR * sin(tickAngle).toFloat()),
                    end = Offset(cx + outerR * cos(tickAngle).toFloat(), cy + outerR * sin(tickAngle).toFloat()),
                    strokeWidth = 1.dp.toPx()
                )
            }

            majorTicks.forEach { tick ->
                val tickAngle = Math.toRadians((startAngle + sweepAngle * (tick / maxSpeed)).toDouble())
                val outerR = radius + strokeWidth * 0.6f
                val innerR = outerR - (strokeWidth * 0.55f)
                drawLine(
                    color = Color(0xFF3B8BFF).copy(alpha = 0.7f),
                    start = Offset(cx + innerR * cos(tickAngle).toFloat(), cy + innerR * sin(tickAngle).toFloat()),
                    end = Offset(cx + outerR * cos(tickAngle).toFloat(), cy + outerR * sin(tickAngle).toFloat()),
                    strokeWidth = 2.5.dp.toPx()
                )

                // 12. Tick labels (OUTSIDE arc, no collision)
                val baseLabelRadius = radius + strokeWidth * 1.5f
                val labelRadius = when(tick) {
                    0f       -> baseLabelRadius + strokeWidth * 0.5f
                    maxSpeed -> baseLabelRadius + strokeWidth * 0.5f
                    else     -> baseLabelRadius
                }
                val xNudge = when(tick) {
                    0f       -> -strokeWidth * 0.3f
                    maxSpeed -> strokeWidth * 0.3f
                    else     -> 0f
                }
                
                // BUG 2 FIX — Add yNudge for bottom corner labels
                val yNudge = when(tick) {
                    0f       -> -strokeWidth * 0.8f
                    maxSpeed -> -strokeWidth * 0.8f
                    else     -> 0f
                }
                
                val labelX = cx + labelRadius * cos(tickAngle).toFloat() + xNudge
                val labelY = cy + labelRadius * sin(tickAngle).toFloat() + yNudge

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#64748B")
                        textSize = 24f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.MONOSPACE
                    }
                    drawText(
                        tick.toInt().toString(),
                        labelX,
                        labelY + paint.textSize / 3f,
                        paint
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .offset(y = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Rolling number
            Text(
                text = String.format(Locale.US, "%.1f", animatedDisplayNum),
                style = TextStyle(
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White,
                    letterSpacing = (-1.5).sp
                )
            )
            
            Spacer(Modifier.height(2.dp))
            
            Text(
                text = "Mbps",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.sp
                )
            )
            
            Spacer(Modifier.height(10.dp))
            
            // Animated status label
            AnimatedContent(
                targetState = statusLabel,
                transitionSpec = {
                    (fadeIn(tween(200)) + slideInVertically { it / 2 })
                        .togetherWith(
                            fadeOut(tween(150)) + 
                            slideOutVertically { -it / 2 }
                        )
                },
                label = "StatusAnimation"
            ) { label ->
                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

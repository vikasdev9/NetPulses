package com.example.netpulse.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalTextApi::class)
@Composable
fun SpeedGauge(
    speedMbps: Float,
    statusLabel: String,
    statusColor: Color,
    isRunning: Boolean,
    modifier: Modifier = Modifier,
    maxSpeed: Float = 100f,
    gaugeColor: Color = MaterialTheme.colorScheme.primary,
    gaugeSecondaryColor: Color = MaterialTheme.colorScheme.secondary,
) {
    val primaryColor = gaugeColor
    val secondaryColor = gaugeSecondaryColor
    val outlineColor = MaterialTheme.colorScheme.outline
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val onBackground = MaterialTheme.colorScheme.onBackground

    val animatedDisplayNum by animateFloatAsState(
        targetValue = speedMbps,
        animationSpec = tween(durationMillis = 250, easing = LinearEasing),
        label = "DisplayNumberAnimation"
    )

    val animatedArcProgress by animateFloatAsState(
        targetValue = (speedMbps / maxSpeed).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "ArcAnimation"
    )

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
        remember { mutableFloatStateOf(1f) }
    }

    val textMeasurer = rememberTextMeasurer()
    val tickTextStyle = TextStyle(
        color = onSurfaceVariant,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h * 0.50f
                val radius = w * 0.38f
                val strokeWidth = w * 0.048f
                
                val startAngle = 135f
                val sweepAngle = 270f
                
                val arcRectTopLeft = Offset(cx - radius, cy - radius)
                val arcRectSize = Size(radius * 2, radius * 2)

                val majorTicks = listOf(0f, 25f, 50f, 75f, 100f)
                val minorTicks = listOf(12.5f, 37.5f, 62.5f, 87.5f)

                onDrawBehind {
                    // 1. Background arc track
                    drawArc(
                        color = outlineColor.copy(alpha = 0.2f),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = arcRectTopLeft,
                        size = arcRectSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    if (animatedArcProgress > 0f) {
                        val currentSweep = sweepAngle * animatedArcProgress

                        // 3. Outer glow arc
                        drawArc(
                            color = primaryColor.copy(alpha = 0.15f),
                            startAngle = startAngle,
                            sweepAngle = currentSweep,
                            useCenter = false,
                            topLeft = arcRectTopLeft,
                            size = arcRectSize,
                            style = Stroke(width = strokeWidth * 2.2f, cap = StrokeCap.Round)
                        )

                        // 4. Mid glow arc
                        drawArc(
                            color = primaryColor.copy(alpha = 0.35f),
                            startAngle = startAngle,
                            sweepAngle = currentSweep,
                            useCenter = false,
                            topLeft = arcRectTopLeft,
                            size = arcRectSize,
                            style = Stroke(width = strokeWidth * 1.5f, cap = StrokeCap.Round)
                        )

                        // 5. Secondary color arc (first half of fill)
                        drawArc(
                            color = secondaryColor,
                            startAngle = startAngle,
                            sweepAngle = currentSweep * 0.5f,
                            useCenter = false,
                            topLeft = arcRectTopLeft,
                            size = arcRectSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // 6. Primary color arc (second half of fill)
                        if (animatedArcProgress > 0.5f) {
                            drawArc(
                                color = primaryColor,
                                startAngle = startAngle + currentSweep * 0.5f,
                                sweepAngle = currentSweep * 0.5f,
                                useCenter = false,
                                topLeft = arcRectTopLeft,
                                size = arcRectSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        val tipAngle = Math.toRadians((startAngle + currentSweep).toDouble())
                        val tipX = cx + radius * cos(tipAngle).toFloat()
                        val tipY = cy + radius * sin(tipAngle).toFloat()
                        val tipOffset = Offset(tipX, tipY)

                        // 7. Tip outer glow circle
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.25f),
                            radius = strokeWidth * 1.1f * pulseScale,
                            center = tipOffset
                        )

                        // 8. Tip mid glow circle
                        drawCircle(
                            color = secondaryColor.copy(alpha = 0.5f),
                            radius = strokeWidth * 0.7f,
                            center = tipOffset
                        )

                        // 9. Tip core dot
                        drawCircle(
                            color = onBackground,
                            radius = strokeWidth * 0.42f,
                            center = tipOffset
                        )
                    }

                    minorTicks.forEach { tick ->
                        val tickAngle = Math.toRadians((startAngle + sweepAngle * (tick / maxSpeed)).toDouble())
                        val outerR = radius + strokeWidth * 0.45f
                        val innerR = outerR - (strokeWidth * 0.3f)
                        drawLine(
                            color = onSurfaceVariant.copy(alpha = 0.8f),
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
                            color = primaryColor.copy(alpha = 0.7f),
                            start = Offset(cx + innerR * cos(tickAngle).toFloat(), cy + innerR * sin(tickAngle).toFloat()),
                            end = Offset(cx + outerR * cos(tickAngle).toFloat(), cy + outerR * sin(tickAngle).toFloat()),
                            strokeWidth = 2.5.dp.toPx()
                        )

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
                        
                        val yNudge = when(tick) {
                            0f       -> -strokeWidth * 0.8f
                            maxSpeed -> -strokeWidth * 0.8f
                            else     -> 0f
                        }
                        
                        val labelX = cx + labelRadius * cos(tickAngle).toFloat() + xNudge
                        val labelY = cy + labelRadius * sin(tickAngle).toFloat() + yNudge

                        val textLayoutResult = textMeasurer.measure(
                            text = tick.toInt().toString(),
                            style = tickTextStyle
                        )
                        
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(labelX - textLayoutResult.size.width / 2f, labelY - textLayoutResult.size.height / 2f)
                        )
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .offset(y = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format(Locale.US, "%.1f", animatedDisplayNum),
                style = TextStyle(
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = onBackground,
                    letterSpacing = (-1.5).sp
                )
            )
            
            Spacer(Modifier.height(2.dp))
            
            Text(
                text = "Mbps",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            )
            
            Spacer(Modifier.height(10.dp))
            
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

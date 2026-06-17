package com.example.netpulse.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp


@Composable
fun RippleEffect() {

    Box(
        modifier = Modifier.size(260.dp),
        contentAlignment = Alignment.Center
    ) {

        Ripple(delay = 0)

        Ripple(delay = 700)

        Ripple(delay = 1400)

        Ripple(delay = 2100)
    }
}

@Composable
fun Ripple(delay: Int) {

    val transition = rememberInfiniteTransition()

    val radius by transition.animateFloat(
        initialValue = .45f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2800,
                delayMillis = delay,
                easing = LinearEasing
            )
        )
    )

    val alpha by transition.animateFloat(
        initialValue = .45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2800,
                delayMillis = delay,
                easing = LinearEasing
            )
        )
    )

    Canvas(
        Modifier.fillMaxSize()
    ) {

        val r = size.minDimension / 5 * radius

        drawCircle(

            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x3300D9FF),
                    Color.Transparent
                ),
                radius = r + 40f
            ),
            radius = r + 40f
        )

        drawCircle(
            color = Color(0xFF00D9FF).copy(alpha),
            radius = r,
            style = Stroke(2.dp.toPx())
        )
    }
}

@Composable
fun GlowingIcon() {

    val transition = rememberInfiniteTransition()

    val scale by transition.animateFloat(
        initialValue = .98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                2200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        contentAlignment = Alignment.Center
    ) {

        Canvas(
            Modifier.size(160.dp)
        ) {

            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        Color(0x3300D9FF),
                        Color.Transparent
                    )
                )
            )
        }

        Box(
            modifier = Modifier
                .scale(scale)
                .size(88.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF2470B3)),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                Icons.Default.Bolt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun AnimatedProgressBar() {

    val transition = rememberInfiniteTransition()

    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Restart
        )
    )

    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth(.72f)
            .height(4.dp)
            .clip(RoundedCornerShape(50)),
        color = Color(0xFF00D9FF),
        trackColor = Color(0xFF15263A)
    )
}
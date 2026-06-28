package com.example.netpulse.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.netpulse.R

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
    val transition = rememberInfiniteTransition(label = "ripple")
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val radius by transition.animateFloat(
        initialValue = .45f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2800,
                delayMillis = delay,
                easing = LinearEasing
            )
        ),
        label = "radius"
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
        ),
        label = "alpha"
    )

    Canvas(
        Modifier.fillMaxSize()
    ) {
        val r = size.minDimension / 5 * radius
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    secondaryColor.copy(alpha = 0.2f),
                    Color.Transparent
                ),
                radius = r + 40f
            ),
            radius = r + 40f
        )

        drawCircle(
            color = secondaryColor.copy(alpha),
            radius = r,
            style = Stroke(2.dp.toPx())
        )
    }
}

@Composable
fun GlowingIcon() {
    val transition = rememberInfiniteTransition(label = "glow")
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val scale by transition.animateFloat(
        initialValue = .98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                2200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
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
                        secondaryColor.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                )
            )
        }

        Box(
            modifier = Modifier
                .scale(scale)
                .size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.netpulsesicon),
                contentDescription = "NetPulse Icon",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
            )
        }
    }
}

@Composable
fun AnimatedProgressBar(delayMillis: Int = 0) {
    val transition = rememberInfiniteTransition(label = "progress")
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val outlineColor = MaterialTheme.colorScheme.outline

    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "progressValue"
    )

    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth(.72f)
            .height(4.dp)
            .clip(RoundedCornerShape(50)),
        color = secondaryColor,
        trackColor = outlineColor.copy(alpha = 0.15f)
    )
}

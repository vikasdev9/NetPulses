package com.example.netpulse.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.R
import com.example.netpulse.ui.components.AnimatedProgressBar
import com.example.netpulse.ui.components.GlowingIcon
import com.example.netpulse.ui.components.RippleEffect
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {

    LaunchedEffect(Unit) {
        delay(2500.milliseconds)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF020A1A),
                        Color(0xFF041127),
                        Color(0xFF000814)
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.weight(1f))

            Box(contentAlignment = Alignment.Center) {

                RippleEffect()

                GlowingIcon()
            }

            Spacer(Modifier.height(26.dp))

            Text(
                "NetPulse",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Fast. Accurate. Beautiful.",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(42.dp))

            AnimatedProgressBar(delayMillis = 0)
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedProgressBar(delayMillis = 300)
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedProgressBar(delayMillis = 600)

            Spacer(Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    Modifier
                        .size(8.dp)
                        .background(
                            Color(0xFF00E676),
                            CircleShape
                        )
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    "Ready to test",
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                "Version 1.0.0",
                color = Color(0xFF666666)
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
package com.example.netpulse.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.netpulse.ui.theme.DarkGradient
import com.example.netpulse.ui.theme.Teal200

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {

    var page by remember { mutableStateOf(0) }

    val pages = listOf(
        "Welcome to Speed Tester",
        "Measure download, upload, and latency",
        "View results and track performance"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGradient)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Spacer(modifier = Modifier.height(1.dp))

            Text(
                text = pages[page],
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {

                pages.forEachIndexed { index, _ ->

                    val active = index == page

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (active) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (active)
                                    Teal200
                                else
                                    Color.White.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextButton(
                    onClick = onFinished
                ) {
                    Text("Skip")
                }

                Button(
                    onClick = {
                        if (page < pages.lastIndex) {
                            page++
                        } else {
                            onFinished()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Teal200,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = if (page < pages.lastIndex)
                            "Next"
                        else
                            "Get Started"
                    )
                }
            }
        }
    }
}
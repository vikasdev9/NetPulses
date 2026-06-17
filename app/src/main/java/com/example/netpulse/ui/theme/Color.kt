package com.example.netpulse.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Background = Color(0xFF0A0E1A)
val CardSurface = Color(0xFF131929)
val CardBorder = Color(0xFF1E2740)
val PrimaryAccent = Color(0xFF3B8BFF)
val GaugeCyan = Color(0xFF00D4FF)
val GaugeBlue = Color(0xFF3B8BFF)
val GaugeTrack = Color(0xFF1E2740)

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF8892A4)

val GreenAccentBg = Color(0xFF0D2E1A)
val GreenAccentIcon = Color(0xFF00E676)

val AmberAccentBg = Color(0xFF2E1F0D)
val AmberAccentIcon = Color(0xFFFFB300)

val BlueAccentBg = Color(0xFF0D1E2E)
val BlueAccentIcon = Color(0xFF3B8BFF)

val CyanAccentBg = Color(0xFF0D2A2E)
val CyanAccentIcon = Color(0xFF00D4FF)

val StatusCyan = Color(0xFF00D4FF)
val LiveBadgeBg = Color(0xFF00D4FF)

// Missing colors and gradients
val Teal200 = Color(0xFF00D4FF) // Using GaugeCyan as reference
val DarkColor = Background
val DarkColor2 = CardSurface
val LightColor = TextPrimary
val LightColor2 = TextSecondary

val DarkGradient = Brush.verticalGradient(
    colors = listOf(Background, CardSurface)
)

val IndigoGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1A1F3A), Color(0xFF2A3050))
)

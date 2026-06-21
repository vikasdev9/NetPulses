package com.example.netpulse.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Dark Theme Colors
val DarkBackground = Color(0xFF0A0E1A)
val DarkSurface = Color(0xFF131929)
val DarkSurfaceVariant = Color(0xFF1E2740)
val DarkPrimary = Color(0xFF3B8BFF)
val DarkSecondary = Color(0xFF00D4FF)
val DarkTertiary = Color(0xFF00E676)
val DarkOnBackground = Color(0xFFFFFFFF)
val DarkOnSurface = Color(0xFFFFFFFF)
val DarkOnSurfaceVariant = Color(0xFF8892A4)
val DarkOutline = Color(0xFF1E2740)

// Light Theme Colors
val LightBackground = Color(0xFFF0F4FF)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE8EDF8)
val LightPrimary = Color(0xFF2563EB)
val LightSecondary = Color(0xFF0891B2)
val LightTertiary = Color(0xFF059669)
val LightOnBackground = Color(0xFF0A0E1A)
val LightOnSurface = Color(0xFF0A0E1A)
val LightOnSurfaceVariant = Color(0xFF475569)
val LightOutline = Color(0xFFCBD5E1)

// Common / Accent Colors (Can be used with alpha or directly)
val AmberAccentIcon = Color(0xFFFFB300)
val AmberAccentBg = Color(0xFF2E1F0D)
val GreenAccentBg = Color(0xFF0D2E1A)
val BlueAccentBg = Color(0xFF0D1E2E)
val CyanAccentBg = Color(0xFF0D2A2E)

// Legacy Compatibility (FOR REFACTORING)
val Background = DarkBackground
val CardSurface = DarkSurface
val CardBorder = DarkSurfaceVariant
val PrimaryAccent = DarkPrimary
val GaugeCyan = DarkSecondary
val GaugeBlue = DarkPrimary
val GaugeTrack = DarkSurfaceVariant
val TextPrimary = DarkOnBackground
val TextSecondary = DarkOnSurfaceVariant

// Missing legacy tokens to fix build errors
val Teal200 = DarkSecondary
val DarkColor = DarkBackground
val DarkColor2 = DarkSurface
val LightColor = DarkOnBackground
val LightColor2 = DarkOnSurfaceVariant
val StatusCyan = DarkSecondary
val LiveBadgeBg = DarkSecondary
val GreenAccentIcon = DarkTertiary
val CyanAccentIcon = DarkSecondary

val DarkGradient = Brush.verticalGradient(
    colors = listOf(DarkBackground, DarkSurface)
)

val IndigoGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1A1F3A), Color(0xFF2A3050))
)

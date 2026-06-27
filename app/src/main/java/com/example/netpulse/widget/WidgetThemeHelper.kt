package com.example.netpulse.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider

object WidgetThemeHelper {
    fun getBackgroundColor(theme: String): ColorProvider {
        return when (theme) {
            "Light" -> androidx.glance.unit.ColorProvider(Color(0xFFF0F4FF))
            "Dark" -> androidx.glance.unit.ColorProvider(Color(0xFF0A0E1A))
            else -> androidx.glance.color.ColorProvider(day = Color(0xFFF0F4FF), night = Color(0xFF0A0E1A))
        }
    }
    
    fun getSurfaceColor(theme: String): ColorProvider {
        return when (theme) {
            "Light" -> androidx.glance.unit.ColorProvider(Color(0xFFFFFFFF))
            "Dark" -> androidx.glance.unit.ColorProvider(Color(0xFF131929))
            else -> androidx.glance.color.ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF131929))
        }
    }

    fun getTextPrimary(theme: String): ColorProvider {
        return when (theme) {
            "Light" -> androidx.glance.unit.ColorProvider(Color(0xFF0A0E1A))
            "Dark" -> androidx.glance.unit.ColorProvider(Color.White)
            else -> androidx.glance.color.ColorProvider(day = Color(0xFF0A0E1A), night = Color.White)
        }
    }

    fun getTextSecondary(theme: String): ColorProvider {
        return when (theme) {
            "Light" -> androidx.glance.unit.ColorProvider(Color(0xFF475569))
            "Dark" -> androidx.glance.unit.ColorProvider(Color(0xFF8892A4))
            else -> androidx.glance.color.ColorProvider(day = Color(0xFF475569), night = Color(0xFF8892A4))
        }
    }
}

package com.example.netpulse.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.netpulse.MainActivity
import java.util.Locale

class LiveSpeedWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetDataStore.loadWidgetData(context)
        provideContent {
            LiveSpeedContent(data)
        }
    }

    @Composable
    private fun LiveSpeedContent(data: WidgetData) {
        val backgroundColor = WidgetThemeHelper.getBackgroundColor(data.theme)
        val textPrimary = WidgetThemeHelper.getTextPrimary(data.theme)
        val textSecondary = WidgetThemeHelper.getTextSecondary(data.theme)
        
        val blueAccent = androidx.glance.color.ColorProvider(day = Color(0xFF2563EB), night = Color(0xFF3B8BFF))
        val cyanAccent = androidx.glance.color.ColorProvider(day = Color(0xFF0891B2), night = Color(0xFF00D4FF))
        val greenAccent = androidx.glance.color.ColorProvider(day = Color(0xFF059669), night = Color(0xFF00E676))

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundColor)
                .cornerRadius(24.dp)
                .padding(16.dp)
                .clickable(actionStartActivity<MainActivity>()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Speed",
                    style = TextStyle(color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = data.networkType,
                    style = TextStyle(color = cyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            // Main Download Value
            Text(
                text = String.format(Locale.US, "%.1f", data.downloadMbps),
                style = TextStyle(color = textPrimary, fontSize = 42.sp, fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Mbps Download",
                style = TextStyle(color = blueAccent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            )

            Spacer(modifier = GlanceModifier.height(16.dp))

            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Spacer(modifier = GlanceModifier.defaultWeight())
                StatCircle("Upload", String.format(Locale.US, "%.1f", data.uploadMbps), cyanAccent)
                Spacer(modifier = GlanceModifier.defaultWeight())
                StatCircle("Ping", "${data.pingMs}ms", greenAccent)
                Spacer(modifier = GlanceModifier.defaultWeight())
            }
        }
    }

    @Composable
    private fun StatCircle(label: String, value: String, color: androidx.glance.unit.ColorProvider) {
        val textSecondary = androidx.glance.color.ColorProvider(day = Color(0xFF475569), night = Color(0xFF8892A4))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, style = TextStyle(color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp))
            Text(text = label, style = TextStyle(color = textSecondary, fontSize = 10.sp))
        }
    }
}

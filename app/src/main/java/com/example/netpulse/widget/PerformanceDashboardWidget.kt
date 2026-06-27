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

class PerformanceDashboardWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetDataStore.loadWidgetData(context)
        provideContent {
            DashboardContent(data)
        }
    }

    @Composable
    private fun DashboardContent(data: WidgetData) {
        val backgroundColor = WidgetThemeHelper.getBackgroundColor(data.theme)
        val surfaceColor = WidgetThemeHelper.getSurfaceColor(data.theme)
        val textPrimary = WidgetThemeHelper.getTextPrimary(data.theme)
        val textSecondary = WidgetThemeHelper.getTextSecondary(data.theme)
        
        val primaryColor = androidx.glance.color.ColorProvider(day = Color(0xFF2563EB), night = Color(0xFF3B8BFF))
        val secondaryColor = androidx.glance.color.ColorProvider(day = Color(0xFF0891B2), night = Color(0xFF00D4FF))

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundColor)
                .cornerRadius(28.dp)
                .padding(16.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Performance",
                    style = TextStyle(color = primaryColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    data.lastTestedLabel,
                    style = TextStyle(color = textSecondary, fontSize = 10.sp)
                )
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        String.format(Locale.US, "%.1f", data.downloadMbps),
                        style = TextStyle(color = textPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    )
                    Text("Mbps Down", style = TextStyle(color = textSecondary, fontSize = 10.sp))
                }
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        String.format(Locale.US, "%.1f", data.uploadMbps),
                        style = TextStyle(color = textPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    )
                    Text("Mbps Up", style = TextStyle(color = textSecondary, fontSize = 10.sp))
                }
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            Row(modifier = GlanceModifier.fillMaxWidth().background(surfaceColor).cornerRadius(16.dp).padding(10.dp)) {
                DashboardMetric("Gaming", data.gamingRating, primaryColor, GlanceModifier.defaultWeight())
                DashboardMetric("Streaming", data.streamingRating, secondaryColor, GlanceModifier.defaultWeight())
                DashboardMetric("Ping", "${data.pingMs}ms", textPrimary, GlanceModifier.defaultWeight())
            }
        }
    }

    @Composable
    private fun DashboardMetric(label: String, value: String, valueColor: androidx.glance.unit.ColorProvider, modifier: GlanceModifier) {
        val textSecondary = androidx.glance.color.ColorProvider(day = Color(0xFF475569), night = Color(0xFF8892A4))
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = TextStyle(color = textSecondary, fontSize = 9.sp))
            Text(value, style = TextStyle(color = valueColor, fontSize = 11.sp, fontWeight = FontWeight.Bold))
        }
    }
}

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

class MiniAnalyticsWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetDataStore.loadWidgetData(context)
        provideContent {
            MiniContent(data)
        }
    }

    @Composable
    private fun MiniContent(data: WidgetData) {
        val backgroundColor = WidgetThemeHelper.getBackgroundColor(data.theme)
        val textPrimary = WidgetThemeHelper.getTextPrimary(data.theme)
        val textSecondary = WidgetThemeHelper.getTextSecondary(data.theme)
        val primaryColor = androidx.glance.color.ColorProvider(day = Color(0xFF2563EB), night = Color(0xFF3B8BFF))

        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundColor)
                .cornerRadius(20.dp)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .size(44.dp)
                    .background(primaryColor)
                    .cornerRadius(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${data.healthScore}",
                    style = TextStyle(color = androidx.glance.color.ColorProvider(day = Color.White, night = Color.White), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                )
            }
            
            Spacer(modifier = GlanceModifier.width(12.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = data.networkType,
                    style = TextStyle(color = textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Usage: ${data.usageToday}",
                    style = TextStyle(color = textSecondary, fontSize = 10.sp)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${data.batteryPct}%",
                    style = TextStyle(color = textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                )
                Text(
                    text = data.lastTestedLabel,
                    style = TextStyle(color = textSecondary, fontSize = 9.sp)
                )
            }
        }
    }
}

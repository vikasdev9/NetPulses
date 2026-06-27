package com.example.netpulse.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
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

class QuickSpeedTestWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetDataStore.loadWidgetData(context)
        
        provideContent {
            QuickSpeedTestContent(data)
        }
    }

    @Composable
    private fun QuickSpeedTestContent(data: WidgetData) {
        val backgroundColor = WidgetThemeHelper.getBackgroundColor(data.theme)
        val surfaceColor = WidgetThemeHelper.getSurfaceColor(data.theme)
        val textPrimary = WidgetThemeHelper.getTextPrimary(data.theme)
        val textSecondary = WidgetThemeHelper.getTextSecondary(data.theme)
        val primaryColor = androidx.glance.color.ColorProvider(day = Color(0xFF2563EB), night = Color(0xFF3B8BFF))

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundColor)
                .cornerRadius(24.dp)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = data.wifiName,
                        style = TextStyle(color = textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Last: ${data.lastTestedLabel}",
                        style = TextStyle(color = textSecondary, fontSize = 10.sp)
                    )
                }
                Box(
                    modifier = GlanceModifier
                        .size(40.dp)
                        .background(surfaceColor)
                        .cornerRadius(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${data.healthScore}",
                        style = TextStyle(color = primaryColor, fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(16.dp))

            // START BUTTON
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(primaryColor)
                    .cornerRadius(24.dp)
                    .clickable(actionStartActivity<MainActivity>(
                        parameters = actionParametersOf(ActionParameters.Key<Boolean>("START_TEST") to true)
                    )),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "START TEST",
                    style = TextStyle(color = androidx.glance.color.ColorProvider(day = Color.White, night = Color.White), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                )
            }

            Spacer(modifier = GlanceModifier.height(16.dp))

            Row(modifier = GlanceModifier.fillMaxWidth()) {
                StatItem("Download", "%.1f".format(data.downloadMbps), "Mbps", GlanceModifier.defaultWeight())
                StatItem("Upload", "%.1f".format(data.uploadMbps), "Mbps", GlanceModifier.defaultWeight())
                StatItem("Ping", "${data.pingMs}", "ms", GlanceModifier.defaultWeight())
            }
        }
    }

    @Composable
    private fun StatItem(label: String, value: String, unit: String, modifier: GlanceModifier) {
        val textPrimary = androidx.glance.color.ColorProvider(day = Color(0xFF0A0E1A), night = Color.White)
        val textSecondary = androidx.glance.color.ColorProvider(day = Color(0xFF475569), night = Color(0xFF8892A4))
        
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, style = TextStyle(color = textSecondary, fontSize = 10.sp))
            Text(text = value, style = TextStyle(color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp))
            Text(text = unit, style = TextStyle(color = textSecondary, fontSize = 9.sp))
        }
    }
}

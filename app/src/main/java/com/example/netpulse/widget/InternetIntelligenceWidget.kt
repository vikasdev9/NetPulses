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

class InternetIntelligenceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetDataStore.loadWidgetData(context)
        provideContent {
            IntelligenceContent(data)
        }
    }

    @Composable
    private fun IntelligenceContent(data: WidgetData) {
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
                .padding(16.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Internet Intelligence",
                    style = TextStyle(color = textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Box(
                    modifier = GlanceModifier
                        .background(androidx.glance.color.ColorProvider(day = Color(0x1A2563EB), night = Color(0x1A3B8BFF)))
                        .cornerRadius(12.dp)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${data.healthScore}",
                        style = TextStyle(color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(surfaceColor)
                    .cornerRadius(16.dp)
                    .padding(12.dp)
            ) {
                InfoRow("Public IP", data.publicIp, textSecondary, textPrimary)
                InfoRow("Network", data.networkType, textSecondary, textPrimary)
                InfoRow("ISP", data.isp, textSecondary, textPrimary)
                InfoRow("VPN", if (data.vpnStatus) "Active" else "Inactive", textSecondary, if (data.vpnStatus) primaryColor else textPrimary)
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            Text(
                text = "Last updated: ${data.lastTestedLabel}",
                style = TextStyle(color = textSecondary, fontSize = 9.sp),
                modifier = GlanceModifier.fillMaxWidth()
            )
        }
    }

    @Composable
    private fun InfoRow(label: String, value: String, labelColor: androidx.glance.unit.ColorProvider, valueColor: androidx.glance.unit.ColorProvider) {
        Row(modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp)) {
            Text(text = label, style = TextStyle(color = labelColor, fontSize = 11.sp))
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(text = value, style = TextStyle(color = valueColor, fontSize = 11.sp, fontWeight = FontWeight.Medium))
        }
    }
}

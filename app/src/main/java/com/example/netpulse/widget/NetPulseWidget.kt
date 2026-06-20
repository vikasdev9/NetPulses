package com.example.netpulse.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class NetPulseWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetDataStore.loadWidgetData(context)
        
        provideContent {
            val size = LocalSize.current
            Column(modifier = GlanceModifier.fillMaxSize()) {
                when {
                    size.width < 120.dp -> SmallWidget(data)
                    size.width < 220.dp -> MediumWidget(data)
                    else -> LargeWidget(data)
                }
            }
        }
    }

    @Composable
    private fun SmallWidget(data: WidgetData) {
        Column(
            modifier = GlanceModifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val value = if (data.state == WidgetState.NO_DATA) "—" else "%.1f".format(data.downloadMbps)
            Text(text = "$value Mbps", style = TextStyle(fontWeight = FontWeight.Bold, color = ColorProvider(Color.White)))
            Text(text = "Download", style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color.White.copy(alpha = 0.6f))))
        }
    }

    @Composable
    private fun MediumWidget(data: WidgetData) {
        Column(
            modifier = GlanceModifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val value = if (data.state == WidgetState.NO_DATA) "—" else "%.1f".format(data.downloadMbps)
            Text(text = value, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White)))
            Text(text = "Mbps Download", style = TextStyle(fontSize = 12.sp, color = ColorProvider(Color.Cyan)))
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(text = "Last: ${data.lastTestedLabel}", style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color.White.copy(alpha = 0.5f))))
        }
    }

    @Composable
    private fun LargeWidget(data: WidgetData) {
        Column(modifier = GlanceModifier.fillMaxSize().padding(16.dp)) {
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Text(text = "NetPulse", style = TextStyle(fontWeight = FontWeight.Bold, color = ColorProvider(Color.Cyan)))
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(text = data.networkType, style = TextStyle(fontSize = 12.sp, color = ColorProvider(Color.White.copy(alpha = 0.6f))))
            }
            Spacer(modifier = GlanceModifier.height(16.dp))
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                WidgetStatItem("DOWNLOAD", if (data.state == WidgetState.NO_DATA) "—" else "%.1f".format(data.downloadMbps), "Mbps", GlanceModifier.defaultWeight())
                WidgetStatItem("UPLOAD", if (data.state == WidgetState.NO_DATA) "—" else "%.1f".format(data.uploadMbps), "Mbps", GlanceModifier.defaultWeight())
            }
            Spacer(modifier = GlanceModifier.height(12.dp))
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                WidgetStatItem("PING", if (data.state == WidgetState.NO_DATA) "—" else "${data.pingMs}", "ms", GlanceModifier.defaultWeight())
                WidgetStatItem("JITTER", if (data.state == WidgetState.NO_DATA) "—" else "${data.jitterMs}", "ms", GlanceModifier.defaultWeight())
            }
        }
    }

    @Composable
    private fun WidgetStatItem(label: String, value: String, unit: String, modifier: GlanceModifier) {
        Column(modifier = modifier) {
            Text(text = label, style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color.White.copy(alpha = 0.5f))))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = value, style = TextStyle(fontWeight = FontWeight.Bold, color = ColorProvider(Color.White)))
                Text(text = " $unit", style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color.White.copy(alpha = 0.5f))))
            }
        }
    }
}

package com.example.netpulse.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.netpulse.MainActivity
import com.example.netpulse.R
import java.util.Locale

@Composable
fun LargeWidget(data: WidgetData) {
    // Using fully qualified factory functions to avoid restricted API issues in Glance 1.0.0
    val backgroundColor = androidx.glance.color.ColorProvider(day = Color(0xFF0A0E1A), night = Color(0xFF0A0E1A))
    val surfaceColor = androidx.glance.color.ColorProvider(day = Color(0xFF131929), night = Color(0xFF131929))
    val textPrimary = androidx.glance.color.ColorProvider(day = Color.White, night = Color.White)
    val textSecondary = androidx.glance.color.ColorProvider(day = Color(0xFF8892A4), night = Color(0xFF8892A4))
    
    val blueAccent = androidx.glance.color.ColorProvider(day = Color(0xFF3B8BFF), night = Color(0xFF3B8BFF))
    val cyanAccent = androidx.glance.color.ColorProvider(day = Color(0xFF00D4FF), night = Color(0xFF00D4FF))
    val greenAccent = androidx.glance.color.ColorProvider(day = Color(0xFF00E676), night = Color(0xFF00E676))
    val amberAccent = androidx.glance.color.ColorProvider(day = Color(0xFFFFB300), night = Color(0xFFFFB300))

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(20.dp)
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
            // LEFT COLUMN
            Column(
                modifier = GlanceModifier.defaultWeight()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_bolt),
                        contentDescription = null,
                        modifier = GlanceModifier.size(16.dp)
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    Text(
                        "NetPulse",
                        style = TextStyle(
                            color = textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(GlanceModifier.height(8.dp))
                Row(verticalAlignment = Alignment.Vertical.Bottom) {
                    Text(
                        String.format(Locale.US, "%.1f", data.downloadMbps),
                        style = TextStyle(
                            color = textPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(GlanceModifier.width(4.dp))
                    Text(
                        "Mbps",
                        style = TextStyle(
                            color = textSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text = if (data.state == WidgetState.LOADING) "DOWNLOADING ↓" 
                           else if (data.state == WidgetState.HAS_DATA) "LAST TEST" 
                           else "",
                    style = TextStyle(
                        color = if (data.state == WidgetState.LOADING) cyanAccent else textSecondary,
                        fontSize = 10.sp
                    )
                )
                Text(
                    text = if (data.state == WidgetState.HAS_DATA) "Today, ${data.lastTestedLabel}" else "No data yet",
                    style = TextStyle(
                        color = textSecondary,
                        fontSize = 10.sp
                    )
                )
            }

            Spacer(GlanceModifier.width(12.dp))

            // RIGHT COLUMN
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .background(surfaceColor)
                    .cornerRadius(14.dp)
                    .padding(10.dp)
            ) {
                Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                    LargeMetricCell("↓ Download", String.format(Locale.US, "%.1f", data.downloadMbps), blueAccent, GlanceModifier.defaultWeight())
                    LargeMetricCell("↑ Upload", String.format(Locale.US, "%.1f", data.uploadMbps), cyanAccent, GlanceModifier.defaultWeight())
                }
                Spacer(GlanceModifier.height(8.dp))
                Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                    LargeMetricCell("◎ Ping", "${data.pingMs} ms", greenAccent, GlanceModifier.defaultWeight())
                    LargeMetricCell("≋ Jitter", "${data.jitterMs} ms", amberAccent, GlanceModifier.defaultWeight())
                }
            }
        }
        
        Spacer(GlanceModifier.height(8.dp))
        
        // FOOTER
        Text(
            text = "Tap anywhere to run a new test",
            modifier = GlanceModifier.fillMaxWidth(),
            style = TextStyle(
                color = textSecondary,
                fontSize = 10.sp,
                textAlign = androidx.glance.text.TextAlign.Center
            )
        )
    }
}

@Composable
private fun LargeMetricCell(label: String, value: String, colorProvider: ColorProvider, modifier: GlanceModifier) {
    Column(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = TextStyle(
                color = colorProvider,
                fontSize = 9.sp
            )
        )
        Spacer(GlanceModifier.height(2.dp))
        Text(
            value,
            style = TextStyle(
                color = androidx.glance.color.ColorProvider(day = Color.White, night = Color.White),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

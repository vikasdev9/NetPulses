package com.example.netpulse.widget

import androidx.compose.runtime.Composable
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
fun MediumWidget(data: WidgetData) {
    val backgroundColor = ColorProvider(R.color.widget_background)
    val textPrimary = ColorProvider(R.color.white)
    val textSecondary = ColorProvider(R.color.widget_text_secondary)
    val blueAccent = ColorProvider(R.color.widget_blue)
    val cyanAccent = ColorProvider(R.color.widget_cyan)
    val greenAccent = ColorProvider(R.color.widget_green)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(20.dp)
            .padding(14.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        // TOP ROW
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    provider = ImageProvider(R.drawable.ic_bolt),
                    contentDescription = null,
                    modifier = GlanceModifier.size(14.dp)
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                Text("NetPulse", 
                    style = TextStyle(
                        color = textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(GlanceModifier.defaultWeight())
            Text(
                text = if (data.state == WidgetState.HAS_DATA) data.lastTestedLabel else "--:--",
                style = TextStyle(
                    color = textSecondary,
                    fontSize = 9.sp
                )
            )
        }

        Spacer(GlanceModifier.height(6.dp))

        // SPEED VALUE
        Row(verticalAlignment = Alignment.Vertical.Bottom) {
            Text(
                text = if (data.state == WidgetState.HAS_DATA)
                    String.format(Locale.US, "%.1f", data.downloadMbps)
                else "—",
                style = TextStyle(
                    color = textPrimary,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.width(4.dp))
            Text(
                "Mbps",
                style = TextStyle(
                    color = textSecondary,
                    fontSize = 12.sp
                )
            )
        }

        Spacer(GlanceModifier.height(8.dp))

        // METRICS ROW
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            MetricChip("↓ ${String.format(Locale.US, "%.1f", data.downloadMbps)}", blueAccent)
            Spacer(GlanceModifier.width(4.dp))
            MetricChip("↑ ${String.format(Locale.US, "%.1f", data.uploadMbps)}", cyanAccent)
            Spacer(GlanceModifier.width(4.dp))
            MetricChip("⬤ ${data.pingMs}ms", greenAccent)
        }

        Spacer(GlanceModifier.defaultWeight())

        // BOTTOM ROW
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    provider = ImageProvider(R.drawable.wifi),
                    contentDescription = null,
                    modifier = GlanceModifier.size(12.dp)
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                Text(
                    "${data.networkType} · ${data.isp}",
                    style = TextStyle(
                        color = textSecondary,
                        fontSize = 10.sp
                    )
                )
            }
            Spacer(GlanceModifier.defaultWeight())
            Text(
                "Tap to test",
                style = TextStyle(
                    color = blueAccent,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun MetricChip(text: String, colorProvider: ColorProvider) {
    Box(
        modifier = GlanceModifier
            .background(ColorProvider(R.color.widget_border))
            .cornerRadius(8.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text,
            style = TextStyle(
                color = colorProvider,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

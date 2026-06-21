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
fun SmallWidget(data: WidgetData) {
    // Using fully qualified factory functions to avoid restricted API issues in Glance 1.0.0
    val backgroundColor = androidx.glance.color.ColorProvider(day = Color(0xFF0A0E1A), night = Color(0xFF0A0E1A))
    val textPrimary = androidx.glance.color.ColorProvider(day = Color.White, night = Color.White)
    val textSecondary = androidx.glance.color.ColorProvider(day = Color(0xFF8892A4), night = Color(0xFF8892A4))
    val dividerColor = androidx.glance.color.ColorProvider(day = Color(0xFF2E3A50), night = Color(0xFF2E3A50))

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(16.dp)
            .clickable(actionStartActivity<MainActivity>())
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier.fillMaxSize()
        ) {
            // App icon circle
            Box(
                modifier = GlanceModifier
                    .size(28.dp)
                    .cornerRadius(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.netpulsesicon),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = GlanceModifier.width(8.dp))
            
            Text(
                text = "NetPulse",
                style = TextStyle(
                    color = textPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            // Divider
            Box(
                modifier = GlanceModifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(dividerColor)
            ) {}

            Spacer(modifier = GlanceModifier.width(8.dp))
            
            Column(verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (data.state == WidgetState.HAS_DATA)
                            "↓ ${String.format(Locale.US, "%.1f", data.downloadMbps)}"
                        else "↓ —",
                        style = TextStyle(
                            color = textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(2.dp))
                    Text(
                        text = "Mbps",
                        style = TextStyle(
                            color = textSecondary,
                            fontSize = 9.sp
                        )
                    )
                }
                Text(
                    text = if (data.state == WidgetState.HAS_DATA)
                        "Ping ${data.pingMs}ms"
                    else "Tap to test",
                    style = TextStyle(
                        color = textSecondary,
                        fontSize = 9.sp
                    )
                )
            }
        }
    }
}

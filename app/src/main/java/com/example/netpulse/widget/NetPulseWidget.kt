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
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import com.example.netpulse.MainActivity
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class NetPulseWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetDataStore.loadWidgetData(context)
        
        provideContent {
            val size = LocalSize.current
            when {
                size.width < 120.dp -> SmallWidget(data)
                size.width < 220.dp -> MediumWidget(data)
                else -> LargeWidget(data)
            }
        }
    }
}

package com.example.netpulse.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.state.GlanceStateDefinition

class NetPulseWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(110.dp, 50.dp),   // small  2x1
            DpSize(110.dp, 110.dp),  // medium 2x2
            DpSize(250.dp, 110.dp)   // large  4x2
        )
    )

    override val stateDefinition: GlanceStateDefinition<*> = WidgetDataStore.definition

    override suspend fun provideGlance(
        context: Context, 
        id: GlanceId
    ) {
        provideContent {
            val widgetData = currentState<WidgetData>() ?: WidgetData()
            val size = LocalSize.current

            GlanceTheme {
                when {
                    size.width >= 250.dp -> LargeWidget(widgetData)
                    size.height >= 110.dp -> MediumWidget(widgetData)
                    else -> SmallWidget(widgetData)
                }
            }
        }
    }
}

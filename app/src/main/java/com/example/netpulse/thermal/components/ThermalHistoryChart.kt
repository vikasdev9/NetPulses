package com.example.netpulse.thermal.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.thermal.database.ThermalEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ThermalHistoryChart(
    history: List<ThermalEntity>,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) {
        Box(modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
            Text("No history available", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 8.dp)) {
            val width = size.width
            val height = size.height
            val maxTemp = history.maxOf { it.temperature }.coerceAtLeast(40f)
            val minTemp = history.minOf { it.temperature }.coerceAtMost(30f)
            val range = (maxTemp - minTemp).coerceAtLeast(5f)

            val points: List<Offset> = history.takeLast(20).mapIndexed { index, entity ->
                val x = (index.toFloat() / 19f) * width
                val y = height - ((entity.temperature - minTemp) / range) * height
                Offset(x, y)
            }

            // Draw Path
            val path = Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points.first().x, points.first().y)
                    points.forEach { lineTo(it.x, it.y) }
                }
            }

            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Draw Gradient Area
            val fillPath = Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points.first().x, height)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, height)
                    close()
                }
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent)
                )
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            Text(sdf.format(Date(history.first().timestamp)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(sdf.format(Date(history.last().timestamp)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

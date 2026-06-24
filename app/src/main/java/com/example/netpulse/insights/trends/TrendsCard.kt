package com.example.netpulse.insights.trends

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

@Composable
fun TrendsCard(viewModel: TrendsViewModel) {
    val state by viewModel.uiState.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Speed Trends",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                PeriodToggle(
                    isMonthly = state.isMonthly,
                    onToggle = { viewModel.setPeriod(it) }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (state.points.isNotEmpty()) {
                LineChart(
                    points = state.points,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            } else {
                Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    Text("No trend data available", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TrendStat("Highest", "%.1f".format(state.highest))
                TrendStat("Lowest", "%.1f".format(state.lowest))
                TrendStat("Average", "%.1f".format(state.average))
            }
        }
    }
}

@Composable
fun LineChart(points: List<TrendPoint>, modifier: Modifier = Modifier) {
    val pathAnim = remember { Animatable(0f) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val maxVal = max(points.maxOf { it.value }.toFloat(), 10f) * 1.2f

    LaunchedEffect(points) {
        pathAnim.snapTo(0f)
        pathAnim.animateTo(1f, tween(1500))
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val spaceX = width / (points.size - 1).coerceAtLeast(1)
        
        // Horizontal reference lines
        val lines = 4
        for (i in 0..lines) {
            val y = height - (i * (height / lines))
            drawLine(
                color = Color.LightGray.copy(alpha = 0.1f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val path = Path()
        points.forEachIndexed { i, p ->
            val x = i * spaceX
            val y = height - (p.value.toFloat() / maxVal * height)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        // Draw animated path
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(pathAnim.value * 10000f, 10000f), 0f
                )
            )
        )

        // Draw points
        points.forEachIndexed { i, p ->
            val x = i * spaceX
            val y = height - (p.value.toFloat() / maxVal * height)
            if (p.value > 0) {
                drawCircle(
                    color = primaryColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
fun PeriodToggle(isMonthly: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp)
    ) {
        ToggleItem("Weekly", !isMonthly) { onToggle(false) }
        ToggleItem("Monthly", isMonthly) { onToggle(true) }
    }
}

@Composable
fun ToggleItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TrendStat(label: String, value: String) {
    Column {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(" Mbps", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

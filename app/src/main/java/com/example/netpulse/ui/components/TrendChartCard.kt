package com.example.netpulse.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.ui.theme.NetPulseTheme
import com.example.netpulse.ui.viewmodel.TrendPoint
import com.example.netpulse.ui.viewmodel.TrendStats

@Composable
fun TrendChartCard(
    trendData: List<TrendPoint>,
    stats: TrendStats,
    modifier: Modifier = Modifier
) {
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(trendData) {
        animationProgress.animateTo(1f, tween(1000))
    }

    AnalyticsCard(title = "Network Trends", icon = Icons.Default.Timeline, modifier = modifier) {
        if (trendData.isEmpty()) {
            Box(Modifier.height(150.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Not enough data to show trends", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        } else {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val maxVal = trendData.maxOfOrNull { it.value }?.coerceAtLeast(10f) ?: 100f
                        val minVal = 0f
                        
                        val points = trendData.mapIndexed { index, point ->
                            val x = index * (width / (trendData.size - 1))
                            val y = height - ((point.value - minVal) / (maxVal - minVal) * height)
                            Offset(x, y)
                        }

                        // Draw baseline
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.2f),
                            start = Offset(0f, height),
                            end = Offset(width, height),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Draw path
                        if (points.size >= 2) {
                            val path = Path().apply {
                                moveTo(points[0].x, points[0].y)
                                for (i in 1 until points.size) {
                                    lineTo(points[i].x, points[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = primaryColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Draw points
                        points.forEach { point ->
                            drawCircle(
                                color = primaryColor,
                                radius = 4.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TrendStatItem("Highest", "${stats.highest.toInt()} Mbps", stats.highestLabel)
                    TrendStatItem("Lowest", "${stats.lowest.toInt()} Mbps", stats.lowestLabel)
                    TrendStatItem("Average", "${stats.average.toInt()} Mbps", "Period")
                }
            }
        }
    }
}

@Composable
fun TrendStatItem(label: String, value: String, subLabel: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        Text(value, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(subLabel, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
    }
}

@Preview
@Composable
fun TrendChartCardPreview() {
    NetPulseTheme {
        TrendChartCard(
            trendData = listOf(
                TrendPoint("Mon", 45f),
                TrendPoint("Tue", 52f),
                TrendPoint("Wed", 48f),
                TrendPoint("Thu", 65f),
                TrendPoint("Fri", 58f),
                TrendPoint("Sat", 72f),
                TrendPoint("Sun", 68f)
            ),
            stats = TrendStats(72f, 45f, 58f, "Sat", "Mon")
        )
    }
}

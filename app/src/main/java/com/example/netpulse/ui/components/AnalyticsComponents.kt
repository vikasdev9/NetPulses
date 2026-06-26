package com.example.netpulse.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.ui.theme.*
import com.example.netpulse.ui.viewmodel.*

// --- ZONE A: HERO COMPONENTS ---

@Composable
fun InternetHealthHero(
    score: Int,
    trend: Float,
    status: String,
    networkType: String,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateIntAsState(targetValue = score, animationSpec = tween(1000), label = "score")
    val healthColor = when {
        score >= 85 -> MaterialTheme.colorScheme.tertiary
        score >= 60 -> AmberAccentIcon
        else -> MaterialTheme.colorScheme.error
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "INTERNET HEALTH",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(20.dp))

            Box(contentAlignment = Alignment.Center) {
                // Outer Pulse
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
                    label = "scale"
                )
                
                val outlineVariant = MaterialTheme.colorScheme.outlineVariant
                Canvas(modifier = Modifier.size(160.dp)) {
                    drawCircle(
                        color = healthColor.copy(alpha = 0.1f),
                        radius = (size.minDimension / 2) * pulseScale
                    )
                    drawArc(
                        color = outlineVariant,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(12.dp.toPx())
                    )
                    drawArc(
                        color = healthColor,
                        startAngle = -90f,
                        sweepAngle = (animatedScore / 100f) * 360f,
                        useCenter = false,
                        style = Stroke(12.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$animatedScore",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = status.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = healthColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HeroStatItem("Network", networkType, Icons.Default.Router)
                VerticalDivider(modifier = Modifier.height(32.dp))
                HeroStatItem("Trend", "${if(trend >= 0) "+" else ""}${trend.toInt()}%", if(trend >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown)
            }
        }
    }
}

@Composable
private fun HeroStatItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

// --- ZONE B: PERFORMANCE COMPONENTS ---

@Composable
fun LivePerformanceSection(
    speedSummary: SpeedSummary,
    modifier: Modifier = Modifier
) {
    AnalyticsCard(title = "Live Performance", icon = Icons.Default.Timeline, modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CircularMetricIndicator(
                value = speedSummary.ping.toFloat(),
                maxValue = 100f,
                label = "Ping",
                unit = "ms",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            CircularMetricIndicator(
                value = speedSummary.jitter.toFloat(),
                maxValue = 30f,
                label = "Jitter",
                unit = "ms",
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            CircularMetricIndicator(
                value = speedSummary.packetLoss,
                maxValue = 5f,
                label = "Loss",
                unit = "%",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            UsageChart(
                data = listOf(20f, 45f, 30f, 70f, 55f, 90f, 80f), // Mock for waveform
                modifier = Modifier.fillMaxSize().padding(12.dp)
            )
        }
    }
}

@Composable
fun ConnectionQualityRow(rating: UseCaseRating) {
    Column {
        Text(
            text = "CONNECTION QUALITY",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QualityChip("Gaming", rating.gaming, Icons.Default.SportsEsports)
            QualityChip("Streaming", rating.streaming, Icons.Default.Tv)
            QualityChip("Video Calls", rating.videoCalls, Icons.Default.VideoCall)
            QualityChip("Browsing", rating.browsing, Icons.Default.Language)
            QualityChip("Downloads", rating.downloads, Icons.Default.Download)
        }
    }
}

@Composable
private fun QualityChip(label: String, quality: NetworkQuality, icon: ImageVector) {
    val color = when (quality) {
        NetworkQuality.EXCELLENT -> MaterialTheme.colorScheme.tertiary
        NetworkQuality.GOOD -> MaterialTheme.colorScheme.primary
        NetworkQuality.FAIR -> AmberAccentIcon
        NetworkQuality.POOR -> MaterialTheme.colorScheme.error
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = color)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(quality.label.uppercase(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

// --- NEW ZONE: SUMMARY CARDS ---

@Composable
fun SummaryCard(
    summary: NetworkSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryStatItem("Tests", summary.testCount.toString(), Icons.Default.History, Modifier.weight(1f))
                SummaryStatItem("Avg Down", "${summary.avgDownload.toInt()} Mbps", Icons.Default.Download, Modifier.weight(1.2f))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryStatItem("Avg Ping", "${summary.avgPing} ms", Icons.Default.Timer, Modifier.weight(1f))
                SummaryStatItem("Best Day", summary.bestDay, Icons.Default.CalendarToday, Modifier.weight(1.2f))
            }
        }
    }
}

@Composable
private fun SummaryStatItem(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Box(
            modifier = Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SummaryCardSection(
    summaries: List<NetworkSummary>,
    onSummaryClick: (NetworkSummary) -> Unit
) {
    Column {
        Text(
            text = "PERFORMANCE SUMMARIES",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            summaries.forEach { summary ->
                SummaryCard(
                    summary = summary,
                    onClick = { onSummaryClick(summary) },
                    modifier = Modifier.width(280.dp)
                )
            }
        }
    }
}

// --- ZONE D: TECHNICAL COMPONENTS (EXPANDABLE) ---

@Composable
fun ExpandableAnalyticsCard(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onExpandToggle() }
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    content()
                }
            }
        }
    }
}

// --- SHARED UTILITIES ---

@Composable
fun AnalyticsCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
fun CircularMetricIndicator(
    value: Float,
    maxValue: Float,
    label: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(72.dp)) {
                drawArc(
                    color = color.copy(alpha = 0.1f),
                    startAngle = 140f,
                    sweepAngle = 260f,
                    useCenter = false,
                    style = Stroke(6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
                drawArc(
                    color = color,
                    startAngle = 140f,
                    sweepAngle = ((value / maxValue).coerceIn(0f, 1f)) * 260f,
                    useCenter = false,
                    style = Stroke(6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = value.toInt().toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun UsageChart(
    data: List<Float>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas
        val width = size.width
        val height = size.height
        val spacing = width / (data.size - 1)
        val maxVal = (data.maxOrNull() ?: 1f) * 1.2f
        val points = data.mapIndexed { index, value -> Offset(index * spacing, height - (value / maxVal * height)) }
        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]; val p1 = points[i + 1]
                cubicTo(p0.x + (p1.x - p0.x) / 2f, p0.y, p0.x + (p1.x - p0.x) / 2f, p1.y, p1.x, p1.y)
            }
        }
        drawPath(path = path, color = primaryColor, style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
    }
}

@Composable
fun QualityIndicator(quality: String) {
    val color = when (quality) {
        "Excellent" -> MaterialTheme.colorScheme.tertiary
        "Good" -> MaterialTheme.colorScheme.primary
        "Fair" -> AmberAccentIcon
        else -> MaterialTheme.colorScheme.error
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.1f)).padding(horizontal = 10.dp, vertical = 6.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = quality, color = color, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TimelineItem(event: TimelineEvent) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(event.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text("${event.time} • ${event.description}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun GradientButton(text: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SecondaryActionButton(text: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RangeChip(currentRange: AnalyticsRange, onRangeClick: () -> Unit) {
    Surface(
        onClick = onRangeClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DateRange, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = when(currentRange) {
                    AnalyticsRange.TODAY -> "Today"
                    AnalyticsRange.WEEK -> "This Week"
                    AnalyticsRange.MONTH -> "This Month"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SecurityToggle(label: String, active: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (active) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (active) "ACTIVE" else "INACTIVE",
                color = if (active) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun AppUsageInsightsCard(
    isLoading: Boolean,
    hasPermission: Boolean,
    top3Apps: List<com.example.netpulse.data.analytics.CombinedAppUsage>,
    onShowMore: () -> Unit,
    onGrantAccess: () -> Unit
) {
    AnalyticsCard(title = "App Usage Insights", icon = Icons.Default.Apps) {
        if (!hasPermission) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Icon(Icons.Default.Lock, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Permission Required", style = MaterialTheme.typography.titleSmall)
                Text("Enable usage access to see app stats.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onGrantAccess) { Text("Grant Access") }
            }
        } else {
            top3Apps.forEachIndexed { index, app ->
                AppInsightRow(app = app, usageRatio = 0.7f - (index * 0.2f), showDivider = index < top3Apps.size - 1)
            }
            TextButton(onClick = onShowMore, modifier = Modifier.align(Alignment.End)) {
                Text("View Dashboard ›")
            }
        }
    }
}

@Composable
fun AppInsightRow(
    app: com.example.netpulse.data.analytics.CombinedAppUsage,
    usageRatio: Float,
    showDivider: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
             if (app.appIcon != null) {
                 Image(painter = com.google.accompanist.drawablepainter.rememberDrawablePainter(app.appIcon), contentDescription = null, modifier = Modifier.fillMaxSize())
             } else {
                 Text(app.appName.take(1))
             }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(app.appName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(progress = { usageRatio }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(app.totalBytesFormatted, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
    if (showDivider) HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
}


fun getNextRange(current: AnalyticsRange): AnalyticsRange = when(current) {
    AnalyticsRange.TODAY -> AnalyticsRange.WEEK
    AnalyticsRange.WEEK -> AnalyticsRange.MONTH
    AnalyticsRange.MONTH -> AnalyticsRange.TODAY
}

@Composable
fun getBarColor(index: Int): Color {
    return when(index) {
        0 -> MaterialTheme.colorScheme.primary
        1 -> MaterialTheme.colorScheme.secondary
        2 -> MaterialTheme.colorScheme.tertiary
        3 -> AmberAccentIcon
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }
}

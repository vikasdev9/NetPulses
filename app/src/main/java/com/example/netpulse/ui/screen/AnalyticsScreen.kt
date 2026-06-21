package com.example.netpulse.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.netpulse.data.analytics.CombinedAppUsage
import com.example.netpulse.ui.components.*
import com.example.netpulse.ui.theme.*
import com.example.netpulse.ui.viewmodel.*
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    viewModel: AnalyticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Network Analytics",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (scrollBehavior.state.collapsedFraction < 0.5f) {
                            Text(
                                text = "Real-time network intelligence",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    RangeChip(
                        currentRange = uiState.selectedRange,
                        onRangeClick = { viewModel.setDateRange(getNextRange(uiState.selectedRange)) }
                    )
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. Live Performance Indicators
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CircularMetricIndicator(
                            value = uiState.networkStatus.signalStrength.toFloat(),
                            maxValue = 100f,
                            label = "Signal",
                            unit = "%",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        CircularMetricIndicator(
                            value = uiState.speedSummary.ping.toFloat(),
                            maxValue = 100f,
                            label = "Latency",
                            unit = "ms",
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                        CircularMetricIndicator(
                            value = uiState.speedSummary.jitter.toFloat(),
                            maxValue = 20f,
                            label = "Jitter",
                            unit = "ms",
                            color = AmberAccentIcon,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 2. Current Network Status
                item {
                    AnalyticsCard(title = "Current Network", icon = Icons.Default.Wifi) {
                        QualityIndicator(uiState.networkQuality.label)
                        Spacer(modifier = Modifier.height(16.dp))
                        InfoRow("SSID", uiState.networkStatus.ssid)
                        InfoRow("Security", uiState.networkStatus.security)
                        InfoRow("Frequency", uiState.networkStatus.frequency)
                        InfoRow("Link Speed", "${uiState.networkStatus.linkSpeed} Mbps")
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Traffic", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressRow("TX Speed", uiState.networkStatus.txSpeed.toFloat(), 1000f, MaterialTheme.colorScheme.primary)
                        LinearProgressRow("RX Speed", uiState.networkStatus.rxSpeed.toFloat(), 1000f, MaterialTheme.colorScheme.secondary)
                    }
                }

                // 3. Internet Intelligence
                item {
                    AnalyticsCard(title = "Internet Intelligence", icon = Icons.Default.Public) {
                        InfoRow("Public IP", uiState.internetDetails.publicIp)
                        InfoRow("Local IP", uiState.internetDetails.localIp)
                        InfoRow("ISP", uiState.ispInfo.name)
                        InfoRow("ASN", uiState.ispInfo.asn)
                        InfoRow("Location", "${uiState.ispInfo.city}, ${uiState.ispInfo.country}")
                        InfoRow("Gateway", uiState.internetDetails.gateway)
                    }
                }

                // 4. Traffic Analysis
                item {
                    AnalyticsCard(title = "Traffic Analysis", icon = Icons.Default.BarChart) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SummaryItem(label = "Mobile", value = uiState.mobileData.totalFormatted, modifier = Modifier.weight(1f))
                            SummaryItem(label = "WiFi", value = uiState.wifiData.totalFormatted, modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        DataBarChart(
                            weeklyUsage = uiState.weeklyUsage,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 5. App Usage Insights
                item {
                    AppUsageInsightsCard(
                        isLoading = uiState.isLoading,
                        hasPermission = uiState.hasUsagePermission,
                        top3Apps = uiState.top3Apps,
                        onShowMore = onNavigateToDashboard,
                        onGrantAccess = { viewModel.openUsageSettings() }
                    )
                }

                // 6. Advanced Diagnostics
                item {
                    AnalyticsCard(title = "Advanced Diagnostics", icon = Icons.Default.SettingsSuggest) {
                        InfoRow("MTU Size", uiState.diagnostics.mtu.toString())
                        InfoRow("TCP Latency", "${uiState.diagnostics.tcpLatency} ms")
                        InfoRow("DNS Lookup", "${uiState.diagnostics.dnsLookup} ms")
                        InfoRow("SSL Handshake", "${uiState.diagnostics.handshake} ms")
                        InfoRow("Route Hops", uiState.diagnostics.hops.toString())
                    }
                }

                // 7. Security & Health
                item {
                    AnalyticsCard(title = "Security & Privacy", icon = Icons.Default.Shield) {
                        SecurityToggle("VPN Status", uiState.security.vpnActive)
                        SecurityToggle("Private DNS", uiState.security.privateDns)
                        SecurityToggle("Captive Portal", uiState.security.captivePortal)
                        SecurityToggle("Metered Network", uiState.security.metered)
                    }
                }

                // 8. Connection Timeline
                item {
                    AnalyticsCard(title = "Connection Timeline", icon = Icons.Default.History) {
                        uiState.timeline.take(4).forEach { event ->
                            TimelineItem(event)
                        }
                    }
                }

                // 9. Optimization Suggestions
                item {
                    AnalyticsCard(title = "Optimization Suggestions", icon = Icons.Default.TipsAndUpdates) {
                        uiState.recommendations.forEach { rec ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                    .padding(12.dp)
                            ) {
                                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(rec, color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp)
                            }
                        }
                        
                        // Show tip if present
                        uiState.tipApp?.let { app ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "\uD83D\uDCA1 Data Tip: ${app.appName} consumed high data today.",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // 10. Master Actions
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GradientButton("Run Diagnostics Again", Icons.Default.Refresh, Modifier.fillMaxWidth())
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SecondaryActionButton("Export PDF", Icons.Default.PictureAsPdf, Modifier.weight(1f))
                            SecondaryActionButton("Copy IP", Icons.Default.ContentCopy, Modifier.weight(1f))
                            SecondaryActionButton("Share", Icons.Default.Share, Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        Text(text = value, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LinearProgressRow(label: String, value: Float, max: Float, color: Color) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            Text("${value.toInt()} Mbps", color = MaterialTheme.colorScheme.onBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { value / max },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun SecondaryActionButton(text: String, icon: ImageVector, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = {},
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AppUsageInsightsCard(
    isLoading: Boolean,
    hasPermission: Boolean,
    top3Apps: List<CombinedAppUsage>,
    onShowMore: () -> Unit,
    onGrantAccess: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Apps, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "App Usage Insights",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Show More ›",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onShowMore() }
            )
        }

        if (!hasPermission) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Outlined.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Enable Usage Access to see app insights",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onGrantAccess) {
                    Text("Grant Access", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        } else if (isLoading) {
            repeat(3) { ShimmerAppRow() }
        } else {
            top3Apps.forEachIndexed { index, app ->
                val maxUsage = top3Apps.maxOfOrNull { it.totalBytes } ?: 1L
                AppInsightRow(
                    app = app,
                    usageRatio = if (maxUsage > 0) app.totalBytes.toFloat() / maxUsage else 0f,
                    showDivider = index < top3Apps.size - 1
                )
            }
        }
    }
}

@Composable
fun AppInsightRow(
    app: CombinedAppUsage,
    usageRatio: Float,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: App Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                if (app.appIcon != null) {
                    Image(
                        painter = rememberDrawablePainter(app.appIcon),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = app.appName.take(1).uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // CENTER: App Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    // Time Chip
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(MaterialTheme.colorScheme.outline)
                            .padding(horizontal = 4.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Schedule, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(app.screenTimeFormatted, color = MaterialTheme.colorScheme.tertiary, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Data Chip
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(MaterialTheme.colorScheme.outline)
                            .padding(horizontal = 4.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.SwapVert, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(app.totalBytesFormatted, color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // RIGHT: Vertical Usage Bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outline),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(usageRatio.coerceIn(0.1f, 1f))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 64.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun ShimmerAppRow() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = alpha * 0.1f))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(14.dp)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = alpha * 0.1f), RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = alpha * 0.1f), RoundedCornerShape(99.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = alpha * 0.1f), RoundedCornerShape(99.dp))
                )
            }
        }
    }
}

@Composable
fun RangeChip(currentRange: AnalyticsRange, onRangeClick: () -> Unit) {
    Surface(
        onClick = onRangeClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = when(currentRange) {
                AnalyticsRange.TODAY -> "Today"
                AnalyticsRange.WEEK -> "This Week"
                AnalyticsRange.MONTH -> "This Month"
            },
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

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
import androidx.compose.ui.platform.LocalContext
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
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSummary: (AnalyticsRange) -> Unit = {},
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
                colors = TopAppBarDefaults.topAppBarColors(
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

                // Internet Health Score
                item {
                    InternetHealthHero(
                        score = uiState.healthScore,
                        trend = uiState.healthTrend,
                        status = uiState.networkQuality.label,
                        networkType = uiState.networkStatus.type
                    )
                }

                // Summary Cards
                item {
                    SummaryCardSection(
                        summaries = uiState.summaries,
                        onSummaryClick = { summary -> onNavigateToSummary(summary.range) }
                    )
                }

                // Use Case Ratings
                item {
                    AnalyticsCard(title = "Connection Suitability", icon = Icons.Default.Analytics) {
                        UseCaseRatingRow("Gaming & Low Latency", uiState.useCaseRating.gaming, Icons.Default.SportsEsports)
                        UseCaseRatingRow("4K Streaming", uiState.useCaseRating.streaming, Icons.Default.Tv)
                        UseCaseRatingRow("Video Conferencing", uiState.useCaseRating.videoCalls, Icons.Default.VideoCall)
                        UseCaseRatingRow("Web Browsing", uiState.useCaseRating.browsing, Icons.Default.Language)
                        UseCaseRatingRow("Large Downloads", uiState.useCaseRating.downloads, Icons.Default.Download)
                    }
                }

                // 2. Current Network Status
                item {
                    AnalyticsCard(title = "Current Network", icon = Icons.Default.Wifi) {
                        QualityIndicator(uiState.networkQuality.label)
                        Spacer(modifier = Modifier.height(16.dp))

                        // BASIC INFO
                        InfoRow("Network Type", uiState.networkStatus.type)
                        InfoRow("Connection State", if (uiState.networkStatus.isConnected) "Connected" else "Disconnected")
                        InfoRow("SSID", uiState.networkStatus.ssid)
                        InfoRow("BSSID", uiState.networkStatus.bssid)

                        Spacer(modifier = Modifier.height(12.dp))
                        SectionSubHeader("Addressing & Routing")
                        InfoRow("Local IP", uiState.internetDetails.localIp)
                        InfoRow("Gateway IP", uiState.internetDetails.gateway)
                        InfoRow("Subnet Mask", uiState.internetDetails.subnet)
                        InfoRow("DNS 1", uiState.internetDetails.dns1)
                        InfoRow("DNS 2", uiState.internetDetails.dns2)
                        InfoRow("IPv6 Address", uiState.internetDetails.ipv6)
                        InfoRow("MAC Address", uiState.internetDetails.mac)

                        Spacer(modifier = Modifier.height(12.dp))
                        SectionSubHeader("Link Quality")
                        InfoRow("Wi-Fi Standard", uiState.networkStatus.wifiStandard)
                        InfoRow("Link Speed", "${uiState.networkStatus.linkSpeed} Mbps")
                        InfoRow("Tx Link Speed", "${uiState.networkStatus.txSpeed} Mbps")
                        InfoRow("Rx Link Speed", "${uiState.networkStatus.rxSpeed} Mbps")
                        InfoRow("Signal (RSSI)", "${uiState.networkStatus.rssi} dBm")
                        InfoRow("Signal Level", "${uiState.networkStatus.signalLevel} / 5")
                        InfoRow("Signal Percentage", "${uiState.networkStatus.signalPercentage}%")
                        InfoRow("Frequency", uiState.networkStatus.frequency)
                        InfoRow("Band", uiState.networkStatus.band)
                        InfoRow("Channel", uiState.networkStatus.channel.toString())
                        InfoRow("Security", uiState.networkStatus.security)

                        Spacer(modifier = Modifier.height(12.dp))
                        SectionSubHeader("Environment & Security")
                        InfoRow("Metered", if (uiState.security.metered) "Yes" else "No")
                        InfoRow("VPN Active", if (uiState.security.vpnActive) "Yes" else "No")
                        InfoRow("Captive Portal", if (uiState.security.captivePortal) "Yes" else "No")
                        InfoRow("Hidden Network", if (uiState.networkStatus.isHidden) "Yes" else "No")
                        InfoRow("Randomized MAC", if (uiState.networkStatus.isRandomizedMac) "Enabled" else "Disabled")

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Live Traffic", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressRow("TX Load", uiState.networkStatus.txSpeed.toFloat(), 1000f, MaterialTheme.colorScheme.primary)
                        LinearProgressRow("RX Load", uiState.networkStatus.rxSpeed.toFloat(), 1000f, MaterialTheme.colorScheme.secondary)
                    }
                }

                // 3. Mobile Network Intelligence
                item {
                    AnalyticsCard(title = "Mobile Network", icon = Icons.Default.SignalCellularAlt) {
                        SectionSubHeader("SIM & Carrier")
                        InfoRow("SIM Operator", uiState.mobileNetworkInfo.simOperator)
                        InfoRow("Network Operator", uiState.mobileNetworkInfo.networkOperator)
                        InfoRow("Carrier Name", uiState.mobileNetworkInfo.carrierName)
                        InfoRow("SIM Country", uiState.mobileNetworkInfo.simCountry)
                        InfoRow("MCC", uiState.mobileNetworkInfo.mcc)
                        InfoRow("MNC", uiState.mobileNetworkInfo.mnc)
                        InfoRow("Roaming Status", uiState.mobileNetworkInfo.roamingStatus)

                        Spacer(modifier = Modifier.height(12.dp))
                        SectionSubHeader("Network Technology")
                        InfoRow("Network Generation", uiState.mobileNetworkInfo.networkGeneration)
                        InfoRow("Signal Strength", uiState.mobileNetworkInfo.signalStrength)
                        InfoRow("LTE Signal Strength", uiState.mobileNetworkInfo.lteSignalStrength)
                        InfoRow("NR (5G) Signal Strength", uiState.mobileNetworkInfo.nrSignalStrength)

                        Spacer(modifier = Modifier.height(12.dp))
                        SectionSubHeader("Cell Tower Info")
                        InfoRow("Cell ID", uiState.mobileNetworkInfo.cellId)
                        InfoRow("TAC", uiState.mobileNetworkInfo.tac)
                        InfoRow("PCI", uiState.mobileNetworkInfo.pci)
                        InfoRow("Registered Network", uiState.mobileNetworkInfo.registeredNetwork)
                        InfoRow("Preferred Network Type", uiState.mobileNetworkInfo.preferredNetworkType)
                    }
                }

                // 4. Internet Intelligence
                item {
                    AnalyticsCard(title = "Internet Intelligence", icon = Icons.Default.Public) {
                        InfoRow("Public IP", uiState.internetDetails.publicIp)
                        InfoRow("Local IP", uiState.internetDetails.localIp)
                        InfoRow("ISP", uiState.ispInfo.name)
                        InfoRow("ASN", uiState.ispInfo.asn)
                        InfoRow("Location", "${uiState.ispInfo.city}, ${uiState.ispInfo.country}")
                        InfoRow("Gateway", uiState.internetDetails.gateway)

                        Spacer(modifier = Modifier.height(12.dp))
                        SectionSubHeader("DNS Intelligence")
                        InfoRow("Primary DNS", uiState.internetDetails.dns1)
                        InfoRow("Secondary DNS", uiState.internetDetails.dns2)
                        InfoRow("Private DNS", if (uiState.security.privateDns) "Active (${uiState.security.privateDnsServer})" else "Inactive")
                        InfoRow("DNS Response Time", "${uiState.diagnostics.dnsLookup} ms")
                    }
                }

                // 5. Speed Test Intelligence
                item {
                    AnalyticsCard(title = "Speed Test Intelligence", icon = Icons.Default.Speed) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Last Test Result", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("${uiState.speedSummary.download.toInt()} / ${uiState.speedSummary.upload.toInt()} Mbps", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Ping: ${uiState.speedSummary.ping} ms | Jitter: ${uiState.speedSummary.jitter} ms", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Score: ${uiState.healthScore}",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = uiState.speedSummary.testTime,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        SectionSubHeader("Peak Performance")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            InfoItem("Peak Download", "${uiState.speedSummary.peakDownload.toInt()} Mbps", Modifier.weight(1f))
                            InfoItem("Peak Upload", "${uiState.speedSummary.peakUpload.toInt()} Mbps", Modifier.weight(1f))
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionSubHeader("Average & Stability")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            InfoItem("Avg Download", "${uiState.speedSummary.avgDownload.toInt()} Mbps", Modifier.weight(1f))
                            InfoItem("Avg Upload", "${uiState.speedSummary.avgUpload.toInt()} Mbps", Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            InfoItem("Minimum Ping", "${uiState.speedSummary.minPing} ms", Modifier.weight(1f))
                            InfoItem("Maximum Ping", "${uiState.speedSummary.maxPing} ms", Modifier.weight(1f))
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionSubHeader("Diagnostic Details")
                        InfoRow("Test Duration", uiState.speedSummary.testDuration)
                        InfoRow("Packet Loss", "${String.format(java.util.Locale.US, "%.1f", uiState.speedSummary.packetLoss)}%")
                        InfoRow("Test Server", uiState.speedSummary.server)
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        OutlinedButton(
                            onClick = onNavigateToHistory,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Icon(Icons.Default.History, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Test History", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Trend Data
                item {
                    TrendChartCard(
                        trendData = uiState.trendData,
                        stats = uiState.trendStats
                    )
                }

                // Internet Usage
                item {
                    UsageCard(usage = uiState.estimatedUsage)
                }

                // ISP Performance
                item {
                    IspPerformanceCard(performance = uiState.ispPerformance)
                }

                // Traffic Analysis
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

                // App Usage Insights
                item {
                    AppUsageInsightsCard(
                        isLoading = uiState.isLoading,
                        hasPermission = uiState.hasUsagePermission,
                        top3Apps = uiState.top3Apps,
                        onShowMore = onNavigateToDashboard,
                        onGrantAccess = { viewModel.openUsageSettings() }
                    )
                }

                // Achievement & Streak
                item {
                    AchievementSection(
                        streak = uiState.streak,
                        recentDays = uiState.recentStreakDays,
                        achievements = uiState.achievements
                    )
                }

                // Stability
                item {
                    StabilityCard(metrics = uiState.stabilityMetrics)
                }

                // Advanced Diagnostics
                item {
                    AnalyticsCard(title = "Advanced Network", icon = Icons.Default.SettingsSuggest) {
                        InfoRow("Interface Name", uiState.diagnostics.interfaceName)
                        InfoRow("MTU Size", uiState.diagnostics.mtu.toString())
                        InfoRow("IPv6 Support", if (uiState.diagnostics.ipv6Support) "Active" else "Unavailable")
                        InfoRow("Dual Stack", if (uiState.diagnostics.dualStack) "Enabled" else "Disabled")
                        InfoRow("Estimated RTT", "${uiState.diagnostics.estimatedRtt} ms")
                        InfoRow("Estimated BW", uiState.diagnostics.estimatedBandwidth)
                        InfoRow("Transport Type", uiState.diagnostics.transportType)
                        InfoRow("Validation", uiState.diagnostics.validationStatus)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionSubHeader("Protocol Latency")
                        InfoRow("TCP Latency", "${uiState.diagnostics.tcpLatency} ms")
                        InfoRow("DNS Lookup", "${uiState.diagnostics.dnsLookup} ms")
                        InfoRow("SSL Handshake", "${uiState.diagnostics.handshake} ms")
                    }
                }

                // Security
                item {
                    AnalyticsCard(title = "Security & Privacy", icon = Icons.Default.Shield) {
                        SecurityToggle("VPN Status", uiState.security.vpnActive)
                        SecurityToggle("Private DNS", uiState.security.privateDns)
                        SecurityToggle("Captive Portal", uiState.security.captivePortal)
                        SecurityToggle("Metered Network", uiState.security.metered)
                    }
                }

                // Timeline
                item {
                    AnalyticsCard(title = "Connection Timeline", icon = Icons.Default.History) {
                        uiState.timeline.take(4).forEach { event ->
                            TimelineItem(event)
                        }
                    }
                }

                // Smart Recommendations
                item {
                    RecommendationsSection(
                        recommendations = uiState.recommendations,
                        onDismiss = { /* viewModel.dismissRecommendation(it) */ }
                    )
                }

                // Device Intelligence
                item {
                    AnalyticsCard(title = "Device Intelligence", icon = Icons.Default.Smartphone) {
                        SectionSubHeader("System & Hardware")
                        InfoRow("Brand", uiState.deviceInfo.brand)
                        InfoRow("Manufacturer", uiState.deviceInfo.manufacturer)
                        InfoRow("Model", uiState.deviceInfo.model)
                        InfoRow("CPU Arch", uiState.deviceInfo.cpuArch)
                        InfoRow("CPU Cores", uiState.deviceInfo.cpuCores.toString())
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionSubHeader("Memory & Storage")
                        InfoRow("Total RAM", uiState.deviceInfo.totalRam)
                        InfoRow("Used RAM", uiState.deviceInfo.usedRam)
                        InfoRow("Available RAM", uiState.deviceInfo.availableRam)
                        LinearProgressRow("Storage Usage", uiState.deviceInfo.storageUsagePercent.toFloat(), 100f, MaterialTheme.colorScheme.primary, unit = "%")
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionSubHeader("Battery Information")
                        InfoRow("Battery Level", "${uiState.deviceInfo.batteryLevel}%")
                        InfoRow("Status", if (uiState.deviceInfo.isCharging) "Charging (${uiState.deviceInfo.chargingType})" else "Discharging")
                        InfoRow("Health", uiState.deviceInfo.batteryHealth)
                        InfoRow("Power Save", if (uiState.deviceInfo.isPowerSaveMode) "Active" else "Inactive")
                    }
                }

                // Master Actions
                item {
                    val context = LocalContext.current
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GradientButton(
                            text = "Run Diagnostics Again", 
                            icon = Icons.Default.Refresh, 
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { 
                                android.widget.Toast.makeText(context, "Running diagnostics...", android.widget.Toast.LENGTH_SHORT).show()
                                viewModel.runDiagnosticsAgain() 
                            }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SecondaryActionButton(
                                text = "Export PDF", 
                                icon = Icons.Default.PictureAsPdf, 
                                modifier = Modifier.weight(1f),
                                onClick = { 
                                    android.widget.Toast.makeText(context, "Exporting report...", android.widget.Toast.LENGTH_SHORT).show()
                                    viewModel.exportPdf(context) 
                                }
                            )
                            SecondaryActionButton(
                                text = "Copy IP", 
                                icon = Icons.Default.ContentCopy, 
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.copyIp(context) }
                            )
                            SecondaryActionButton(
                                text = "Share", 
                                icon = Icons.Default.Share, 
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.shareAnalytics(context) }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getNextRange(current: AnalyticsRange): AnalyticsRange = when(current) {
    AnalyticsRange.TODAY -> AnalyticsRange.WEEK
    AnalyticsRange.WEEK -> AnalyticsRange.MONTH
    AnalyticsRange.MONTH -> AnalyticsRange.TODAY
}

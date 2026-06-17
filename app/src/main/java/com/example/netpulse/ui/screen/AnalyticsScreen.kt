package com.example.netpulse.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.netpulse.ui.components.*
import com.example.netpulse.ui.theme.*
import com.example.netpulse.ui.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AnalyticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    Column {
                        Text("Network Analytics", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                        Text("Real-time network intelligence", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Background,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        containerColor = Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
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
                        color = PrimaryAccent,
                        modifier = Modifier.weight(1f)
                    )
                    CircularMetricIndicator(
                        value = uiState.speedSummary.ping.toFloat(),
                        maxValue = 100f,
                        label = "Latency",
                        unit = "ms",
                        color = GreenAccentIcon,
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

            // 2. Network Status
            item {
                AnalyticsCard(title = "Current Network", icon = Icons.Default.Wifi) {
                    QualityIndicator(uiState.networkQuality.label)
                    Spacer(modifier = Modifier.height(16.dp))
                    InfoRow("SSID", uiState.networkStatus.ssid)
                    InfoRow("Security", uiState.networkStatus.security)
                    InfoRow("Frequency", uiState.networkStatus.frequency)
                    InfoRow("Link Speed", "${uiState.networkStatus.linkSpeed} Mbps")
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Traffic", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressRow("TX Speed", uiState.networkStatus.txSpeed.toFloat(), 1000f, BlueAccentIcon)
                    LinearProgressRow("RX Speed", uiState.networkStatus.rxSpeed.toFloat(), 1000f, CyanAccentIcon)
                }
            }

            // 3. Internet & ISP Details
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

            // 4. Data Usage History
            item {
                AnalyticsCard(title = "Traffic Analysis", icon = Icons.Default.BarChart) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        UsageStat("Today", uiState.dataUsage.today)
                        UsageStat("Weekly", uiState.dataUsage.weekly)
                        UsageStat("Monthly", uiState.dataUsage.monthly)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    UsageChart(
                        data = uiState.dataUsage.history,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                }
            }

            // 5. Diagnostics
            item {
                AnalyticsCard(title = "Advanced Diagnostics", icon = Icons.Default.SettingsSuggest) {
                    InfoRow("MTU Size", uiState.diagnostics.mtu.toString())
                    InfoRow("TCP Latency", "${uiState.diagnostics.tcpLatency} ms")
                    InfoRow("DNS Lookup", "${uiState.diagnostics.dnsLookup} ms")
                    InfoRow("SSL Handshake", "${uiState.diagnostics.handshake} ms")
                    InfoRow("Route Hops", uiState.diagnostics.hops.toString())
                }
            }

            // 6. Security & Health
            item {
                AnalyticsCard(title = "Security & Privacy", icon = Icons.Default.Shield) {
                    SecurityToggle("VPN Status", uiState.security.vpnActive)
                    SecurityToggle("Private DNS", uiState.security.privateDns)
                    SecurityToggle("Captive Portal", uiState.security.captivePortal)
                    SecurityToggle("Metered Network", uiState.security.metered)
                }
            }

            // 7. Timeline
            item {
                AnalyticsCard(title = "Connection Timeline", icon = Icons.Default.History) {
                    uiState.timeline.take(4).forEach { event ->
                        TimelineItem(event)
                    }
                }
            }

            // 8. Device Hardware
            item {
                AnalyticsCard(title = "Device Environment", icon = Icons.Default.Smartphone) {
                    InfoRow("Model", "${uiState.deviceInfo.manufacturer} ${uiState.deviceInfo.model}")
                    InfoRow("Android", "${uiState.deviceInfo.androidVersion} (API ${uiState.deviceInfo.sdk})")
                    InfoRow("Memory", uiState.deviceInfo.ram)
                    InfoRow("Storage", uiState.deviceInfo.storage)
                }
            }

            // 9. Recommendations
            item {
                AnalyticsCard(title = "Optimization Suggestions", icon = Icons.Default.TipsAndUpdates) {
                    uiState.recommendations.forEach { rec ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryAccent.copy(alpha = 0.05f))
                                .padding(12.dp)
                        ) {
                            Icon(Icons.Default.Info, null, tint = PrimaryAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(rec, color = TextPrimary, fontSize = 13.sp)
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

@Composable
fun LinearProgressRow(label: String, value: Float, max: Float, color: Color) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = TextSecondary, fontSize = 11.sp)
            Text("${value.toInt()} Mbps", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = value / max,
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
        border = BorderStroke(1.dp, CardBorder),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

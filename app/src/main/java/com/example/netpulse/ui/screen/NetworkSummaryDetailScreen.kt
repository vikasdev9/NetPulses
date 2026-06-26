package com.example.netpulse.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.netpulse.ui.components.*
import com.example.netpulse.ui.viewmodel.*
import com.example.netpulse.utils.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSummaryDetailScreen(
    range: AnalyticsRange,
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    val summary = uiState.summaries.find { it.range == range } ?: NetworkSummary(title = "Summary", range = range)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${summary.title} Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.exportPdf(context) }) {
                        Icon(Icons.Default.Download, "Download PDF")
                    }
                    IconButton(onClick = { viewModel.shareAnalytics(context) }) {
                        Icon(Icons.Default.Share, "Share Report")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                InternetHealthHero(
                    score = uiState.healthScore,
                    trend = uiState.healthTrend,
                    status = uiState.networkQuality.label,
                    networkType = uiState.networkStatus.type
                )
            }

            item {
                AnalyticsCard(title = "Network Statistics", icon = Icons.Outlined.Speed) {
                    InfoRow("Total Tests", summary.testCount.toString())
                    InfoRow("Average Download", "${summary.avgDownload.toInt()} Mbps")
                    InfoRow("Average Upload", "${summary.avgUpload.toInt()} Mbps")
                    InfoRow("Average Ping", "${summary.avgPing} ms")
                    InfoRow("Best Performing Day", summary.bestDay)
                }
            }

            item {
                AnalyticsCard(title = "Traffic Usage", icon = Icons.Outlined.BarChart) {
                    InfoRow("Mobile Data", uiState.mobileData.totalFormatted)
                    InfoRow("WiFi Data", uiState.wifiData.totalFormatted)
                }
            }
            
            item {
                Button(
                    onClick = { 
                        ShareUtils.downloadReportAsImage(context) {
                            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(24.dp)) {
                                InternetHealthHero(
                                    score = uiState.healthScore,
                                    trend = uiState.healthTrend,
                                    status = uiState.networkQuality.label,
                                    networkType = uiState.networkStatus.type
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                AnalyticsCard(title = "Network Statistics", icon = Icons.Outlined.Speed) {
                                    InfoRow("Total Tests", summary.testCount.toString())
                                    InfoRow("Average Download", "${summary.avgDownload.toInt()} Mbps")
                                    InfoRow("Average Upload", "${summary.avgUpload.toInt()} Mbps")
                                    InfoRow("Average Ping", "${summary.avgPing} ms")
                                    InfoRow("Best Performing Day", summary.bestDay)
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Download Report as Image")
                }
            }
        }
    }
}

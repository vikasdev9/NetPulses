package com.example.netpulse.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.data.analytics.CombinedAppUsage
import com.example.netpulse.data.analytics.formatBytes
import com.example.netpulse.data.analytics.formatDuration
import com.example.netpulse.ui.components.DataBarChart
import com.example.netpulse.ui.components.getBarColor
import com.example.netpulse.ui.theme.*
import com.example.netpulse.ui.viewmodel.*
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUsageDashboardScreen(
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "App Usage",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    DashboardTabSelector(
                        selectedTab = uiState.dashboardTab,
                        onTabSelected = { viewModel.setDashboardTab(it) }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            // SECTION 1 — SUMMARY ROW
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniStatCard(
                        value = uiState.mobileData.totalFormatted,
                        label = "Total Data",
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        value = formatDuration(uiState.totalScreenTimeMs),
                        label = "Screen Time",
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        value = uiState.allAppsCombined.size.toString(),
                        label = "Apps Used",
                        accentColor = AmberAccentIcon,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // SECTION 2 — WEEKLY BAR CHART
            item {
                Text(
                    "This Week",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    DataBarChart(weeklyUsage = uiState.weeklyUsage)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // SECTION 3 — ALL APPS LIST
            item {
                Text(
                    "ALL APPS",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            if (uiState.allAppsCombined.isEmpty()) {
                item {
                    EmptyState()
                }
            } else {
                val maxVal = if (uiState.dashboardTab == DashboardTab.DATA) {
                    uiState.allAppsCombined.maxOf { it.totalBytes }.coerceAtLeast(1L)
                } else {
                    uiState.allAppsCombined.maxOf { it.screenTimeMs }.coerceAtLeast(1L)
                }

                itemsIndexed(uiState.allAppsCombined, key = { _, app -> app.packageName }) { index, app ->
                    val currentVal = if (uiState.dashboardTab == DashboardTab.DATA) app.totalBytes else app.screenTimeMs
                    val ratio = currentVal.toFloat() / maxVal

                    DetailedAppUsageRow(
                        index = index,
                        app = app,
                        ratio = ratio,
                        isDataTab = uiState.dashboardTab == DashboardTab.DATA
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun DashboardTabSelector(
    selectedTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(2.dp)
    ) {
        DashboardTabChip(
            label = "Data",
            isSelected = selectedTab == DashboardTab.DATA,
            onClick = { onTabSelected(DashboardTab.DATA) }
        )
        DashboardTabChip(
            label = "Time",
            isSelected = selectedTab == DashboardTab.TIME,
            onClick = { onTabSelected(DashboardTab.TIME) }
        )
    }
}

@Composable
fun DashboardTabChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MiniStatCard(
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = accentColor, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
    }
}

@Composable
fun DetailedAppUsageRow(
    index: Int,
    app: CombinedAppUsage,
    ratio: Float,
    isDataTab: Boolean
) {
    val animatedWidth = remember { Animatable(0f) }
    LaunchedEffect(ratio) {
        delay(index * 50L)
        animatedWidth.animateTo(ratio, tween(400, easing = FastOutSlowInEasing))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: Icon + Rank
            Box(modifier = Modifier.size(40.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (app.appIcon != null) {
                        Image(rememberDrawablePainter(app.appIcon), null, modifier = Modifier.fillMaxSize())
                    } else {
                        Text(app.appName.take(1), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
                
                if (index < 3) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            (index + 1).toString(),
                            color = getBarColor(index),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // CENTER
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(app.appName, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outline)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedWidth.value)
                            .background(getBarColor(index))
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(app.screenTimeFormatted, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Outlined.SwapVert, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(app.totalBytesFormatted, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
            }

            // RIGHT
            Column(horizontalAlignment = Alignment.End) {
                if (isDataTab) {
                    Text(app.totalBytesFormatted, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("↑${formatBytes(app.txBytes)} ↓${formatBytes(app.rxBytes)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                } else {
                    Text(app.screenTimeFormatted, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("Last: ${app.lastUsedLabel}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Outlined.Schedule, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("No app data available", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
        Text("Run a speed test or use your phone first", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}

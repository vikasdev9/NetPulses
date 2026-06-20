package com.example.netpulse.ui.screen

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.example.netpulse.R
import com.example.netpulse.ui.components.*
import com.example.netpulse.ui.theme.*
import com.example.netpulse.ui.viewmodel.*
import kotlin.math.min

import androidx.compose.ui.platform.LocalContext
import com.example.netpulse.NetPulseApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: SpeedTestViewModel = viewModel(factory = SpeedTestViewModel.Factory(application))
    val uiState by viewModel.uiState.collectAsState()
    val ispInfo by viewModel.ispInfo.collectAsState()

    Scaffold(
        topBar = {
            HomeTopBar(onNavigateToAnalytics)
        },
        bottomBar = {
            BottomNavigationBar(onNavigateToHistory, onNavigateToSettings)
        },
        containerColor = Background
    ) { paddingValues ->
        // BoxWithConstraints is the heart of responsive design in Compose.
        // It allows us to calculate component sizes based on the actual screen dimensions.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            val screenHeight = maxHeight
            val screenWidth = maxWidth
            
            // Logic to prevent scrolling: We use a nested Column with weight(1f)
            // for the main content to push the action button towards the bottom.
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top section (Badge, Gauge, Grid) - Spaced evenly to fill available height
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 1. Network Status Badge
                    NetworkBadge()

                    // 2. Speedometer Gauge & 3. Statistics Grid Grouped
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp) // Reduced controlled gap
                    ) {
                        val gaugeSize = min(screenWidth.value * 0.7f, 280f).dp
                        Box(
                            modifier = Modifier.size(gaugeSize),
                            contentAlignment = Alignment.Center
                        ) {
                            val currentSpeed = when (val state = uiState.testState) {
                                is SpeedTestState.Running -> state.currentSpeed
                                is SpeedTestState.Complete -> state.result.downloadMbps.toFloat()
                                else -> 0f
                            }
                            
                            val statusLabel = when (val state = uiState.testState) {
                                is SpeedTestState.Idle -> stringResource(R.string.status_ready)
                                is SpeedTestState.Running -> {
                                    when (state.phase) {
                                        TestPhase.PING -> stringResource(R.string.status_testing_ping)
                                        TestPhase.JITTER -> "MEASURING JITTER…"
                                        TestPhase.DOWNLOAD -> stringResource(R.string.status_downloading)
                                        TestPhase.UPLOAD -> stringResource(R.string.status_uploading)
                                    }
                                }
                                is SpeedTestState.Complete -> stringResource(R.string.status_complete)
                                is SpeedTestState.Error -> "ERROR"
                            }

                            val statusColor = when (uiState.testState) {
                                is SpeedTestState.Idle -> TextSecondary
                                is SpeedTestState.Running -> PrimaryAccent
                                is SpeedTestState.Complete -> GreenAccentIcon
                                is SpeedTestState.Error -> Color.Red
                            }

                            SpeedGauge(
                                speedMbps = currentSpeed,
                                statusLabel = statusLabel,
                                statusColor = statusColor
                            )
                        }

                        // 3. Statistics Grid (Responsive 2x2)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ResponsiveMetricCard(
                                    icon = Icons.Outlined.ArrowDownward,
                                    label = stringResource(R.string.label_download),
                                    value = uiState.download,
                                    unit = stringResource(R.string.label_mbps),
                                    accentColor = BlueAccentIcon,
                                    iconBgColor = BlueAccentBg,
                                    isActive = (uiState.testState as? SpeedTestState.Running)?.phase == TestPhase.DOWNLOAD,
                                    modifier = Modifier.weight(1f)
                                )
                                ResponsiveMetricCard(
                                    icon = Icons.Outlined.ArrowUpward,
                                    label = stringResource(R.string.label_upload),
                                    value = uiState.upload,
                                    unit = stringResource(R.string.label_mbps),
                                    accentColor = CyanAccentIcon,
                                    iconBgColor = CyanAccentBg,
                                    isActive = (uiState.testState as? SpeedTestState.Running)?.phase == TestPhase.UPLOAD,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ResponsiveMetricCard(
                                    icon = Icons.Outlined.Timeline,
                                    label = stringResource(R.string.label_ping),
                                    value = uiState.ping,
                                    unit = stringResource(R.string.label_ms),
                                    accentColor = GreenAccentIcon,
                                    iconBgColor = GreenAccentBg,
                                    isActive = (uiState.testState as? SpeedTestState.Running)?.phase == TestPhase.PING,
                                    modifier = Modifier.weight(1f)
                                )
                                ResponsiveMetricCard(
                                    icon = Icons.Outlined.Waves,
                                    label = stringResource(R.string.label_jitter),
                                    value = uiState.jitter,
                                    unit = stringResource(R.string.label_ms),
                                    accentColor = AmberAccentIcon,
                                    iconBgColor = AmberAccentBg,
                                    isActive = (uiState.testState as? SpeedTestState.Running)?.phase == TestPhase.JITTER,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // 4. Start/Stop Button (Pinned to bottom)
                val isRunning = uiState.testState is SpeedTestState.Running
                ResponsiveActionButton(
                    isRunning = isRunning,
                    isComplete = uiState.testState is SpeedTestState.Complete,
                    onClick = {
                        if (isRunning) viewModel.stopTest() else viewModel.startTest()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Server Info
                Text(
                    text = stringResource(R.string.server_info, "Mumbai", 3),
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
                
                // Controlled spacing from the Bottom Navigation Bar
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(onNavigateToAnalytics: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "NetPulse",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        actions = {
            IconButton(
                onClick = onNavigateToAnalytics,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "Analytics",
                    tint = PrimaryAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Background,
            titleContentColor = TextPrimary
        )
    )
}

@Composable
fun ResponsiveMetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    accentColor: Color,
    iconBgColor: Color,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .then(if (isActive) Modifier.border(1.dp, accentColor, RoundedCornerShape(16.dp)) else Modifier),
        shape = RoundedCornerShape(16.dp),
        color = CardSurface.copy(alpha = 0.6f),
        border = if (!isActive) BorderStroke(1.dp, CardBorder) else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = accentColor, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(label, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun ResponsiveActionButton(
    isRunning: Boolean,
    isComplete: Boolean,
    onClick: () -> Unit
) {
    // We use a Box with drawBehind to create a soft, premium glow 
    // instead of a standard harsh shadow.
    val glowColor = PrimaryAccent.copy(alpha = 0.3f)
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .then(
                if (!isRunning) {
                    Modifier.drawBehind {
                        // Creating a soft blue glow effect without duplicate shapes
                        drawRoundRect(
                            color = glowColor,
                            size = size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(30.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx())
                        )
                    }
                } else Modifier
            ),
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRunning) CardBorder else Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (!isRunning) {
                        Brush.horizontalGradient(listOf(PrimaryAccent, GaugeCyan))
                    } else {
                        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.Bolt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isRunning) stringResource(R.string.btn_stop_test).uppercase() 
                           else if (isComplete) stringResource(R.string.btn_test_again).uppercase() 
                           else stringResource(R.string.btn_start_test).uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(onNavigateToHistory: () -> Unit, onNavigateToSettings: () -> Unit) {
    var selectedItem by remember { mutableStateOf(0) }
    
    NavigationBar(
        containerColor = Background,
        tonalElevation = 0.dp,
        modifier = Modifier.border(0.5.dp, CardBorder, RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
    ) {
        NavigationBarItem(
            selected = selectedItem == 0,
            onClick = { selectedItem = 0 },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text(stringResource(R.string.label_home)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryAccent,
                selectedTextColor = PrimaryAccent,
                unselectedIconColor = TextSecondary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedItem == 1,
            onClick = onNavigateToHistory,
            icon = { Icon(Icons.Default.History, contentDescription = "History") },
            label = { Text(stringResource(R.string.screen_history)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryAccent,
                selectedTextColor = PrimaryAccent,
                unselectedIconColor = TextSecondary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedItem == 2,
            onClick = onNavigateToSettings,
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text(stringResource(R.string.screen_settings)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryAccent,
                selectedTextColor = PrimaryAccent,
                unselectedIconColor = TextSecondary,
                indicatorColor = Color.Transparent
            )
        )
    }
}

package com.example.netpulse.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.netpulse.ui.components.*
import com.example.netpulse.ui.theme.*
import com.example.netpulse.ui.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: SpeedTestViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        bottomBar = {
            BottomNavigationBar()
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "SpeedCheck Pro",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row {
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.History, contentDescription = null, tint = TextSecondary)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.Settings, contentDescription = null, tint = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Network Badge
            NetworkBadge()
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ISP Info
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip(text = "IP: 103.24.xx.xx")
                InfoChip(text = "ISP: Reliance Jio")
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Speedometer Gauge
            val currentSpeed = when (val state = uiState.testState) {
                is SpeedTestState.Running -> state.currentSpeed
                is SpeedTestState.Complete -> state.download
                else -> 0f
            }
            
            val statusLabel = when (val state = uiState.testState) {
                is SpeedTestState.Idle -> "READY"
                is SpeedTestState.Running -> {
                    when (state.phase) {
                        TestPhase.PING -> "MEASURING PING..."
                        TestPhase.JITTER -> "MEASURING JITTER..."
                        TestPhase.DOWNLOAD -> "DOWNLOADING..."
                        TestPhase.UPLOAD -> "UPLOADING..."
                    }
                }
                is SpeedTestState.Complete -> "TEST COMPLETE"
            }

            SpeedGauge(
                speedMbps = currentSpeed,
                statusLabel = statusLabel
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Metric Cards Grid
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MetricCard(
                        icon = Icons.Outlined.ArrowDownward,
                        label = "Download",
                        value = uiState.download,
                        unit = "Mbps",
                        accentColor = BlueAccentIcon,
                        iconBgColor = BlueAccentBg,
                        isActive = (uiState.testState as? SpeedTestState.Running)?.phase == TestPhase.DOWNLOAD,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        icon = Icons.Outlined.ArrowUpward,
                        label = "Upload",
                        value = uiState.upload,
                        unit = "Mbps",
                        accentColor = CyanAccentIcon,
                        iconBgColor = CyanAccentBg,
                        isActive = (uiState.testState as? SpeedTestState.Running)?.phase == TestPhase.UPLOAD,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MetricCard(
                        icon = Icons.Outlined.Timeline,
                        label = "Ping",
                        value = uiState.ping,
                        unit = "ms",
                        accentColor = GreenAccentIcon,
                        iconBgColor = GreenAccentBg,
                        isActive = (uiState.testState as? SpeedTestState.Running)?.phase == TestPhase.PING,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        icon = Icons.Outlined.Waves,
                        label = "Jitter",
                        value = uiState.jitter,
                        unit = "ms",
                        accentColor = AmberAccentIcon,
                        iconBgColor = AmberAccentBg,
                        isActive = (uiState.testState as? SpeedTestState.Running)?.phase == TestPhase.JITTER,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Start/Stop Button
            val isRunning = uiState.testState is SpeedTestState.Running
            Button(
                onClick = {
                    if (isRunning) viewModel.stopTest() else viewModel.startTest()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .then(
                        if (!isRunning) Modifier.shadow(
                            elevation = 12.dp,
                            spotColor = PrimaryAccent.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(28.dp)
                        ) else Modifier
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) CardBorder else Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp),
                border = if (isRunning) BorderStroke(1.dp, Color(0xFF2E3A50)) else null
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (!isRunning) Modifier.background(
                                Brush.horizontalGradient(listOf(PrimaryAccent, GaugeCyan))
                            ) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRunning) "STOP TEST" else if (uiState.testState is SpeedTestState.Complete) "TEST AGAIN" else "START TEST",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Server: Mumbai · 3 connections",
                color = TextSecondary,
                fontSize = 11.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(CardSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(99.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = TextSecondary,
            fontSize = 11.sp
        )
    }
}

@Composable
fun BottomNavigationBar() {
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
            label = { if (selectedItem == 0) Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryAccent,
                selectedTextColor = PrimaryAccent,
                unselectedIconColor = TextSecondary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedItem == 1,
            onClick = { selectedItem = 1 },
            icon = { Icon(Icons.Outlined.History, contentDescription = "History") },
            label = { if (selectedItem == 1) Text("History") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryAccent,
                selectedTextColor = PrimaryAccent,
                unselectedIconColor = TextSecondary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedItem == 2,
            onClick = { selectedItem = 2 },
            icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
            label = { if (selectedItem == 2) Text("Settings") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryAccent,
                selectedTextColor = PrimaryAccent,
                unselectedIconColor = TextSecondary,
                indicatorColor = Color.Transparent
            )
        )
    }
}

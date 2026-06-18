package com.example.netpulse.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.example.netpulse.R
import com.example.netpulse.ui.components.ProUpgradeCard
import com.example.netpulse.ui.components.SegmentedControl
import com.example.netpulse.ui.components.SettingsRow
import com.example.netpulse.ui.theme.*
import com.example.netpulse.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Background,
        bottomBar = {
            SettingsBottomNavigationBar(onNavigateToHome, onNavigateToHistory)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = stringResource(R.string.screen_settings),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // GENERAL SECTION
            SectionLabel(stringResource(R.string.settings_section_general))
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Outlined.Shield,
                        title = stringResource(R.string.settings_dark_mode),
                        trailing = {
                            Switch(
                                checked = state.isDarkMode,
                                onCheckedChange = { viewModel.toggleDarkMode(it) },
                                colors = customSwitchColors()
                            )
                        }
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Language,
                        title = stringResource(R.string.settings_language),
                        trailing = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = state.currentLanguage,
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        },
                        onClick = onNavigateToLanguage
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Notifications,
                        title = stringResource(R.string.settings_notifications),
                        trailing = {
                            Switch(
                                checked = state.notificationsEnabled,
                                onCheckedChange = { viewModel.toggleNotifications(it) },
                                colors = customSwitchColors()
                            )
                        }
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Storage,
                        title = stringResource(R.string.settings_default_server),
                        trailing = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = state.defaultServer,
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        },
                        showDivider = false,
                        onClick = { /* Handle Server Selection */ }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // TEST SETTINGS SECTION
            SectionLabel(stringResource(R.string.settings_section_test))
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Outlined.Timeline,
                        title = stringResource(R.string.settings_parallel_connections),
                        trailing = {
                            SegmentedControl(
                                options = listOf("1", "2", "4"),
                                selectedIndex = when(state.parallelConnections) {
                                    1 -> 0
                                    2 -> 1
                                    else -> 2
                                },
                                onSelectionChanged = { index ->
                                    viewModel.setParallelConnections(when(index) {
                                        0 -> 1
                                        1 -> 2
                                        else -> 4
                                    })
                                }
                            )
                        }
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Timer,
                        title = stringResource(R.string.settings_test_duration),
                        trailing = {
                            SegmentedControl(
                                options = listOf("10s", "20s", "30s"),
                                selectedIndex = when(state.testDurationSeconds) {
                                    10 -> 0
                                    20 -> 1
                                    else -> 2
                                },
                                onSelectionChanged = { index ->
                                    viewModel.setTestDuration(when(index) {
                                        0 -> 10
                                        1 -> 20
                                        else -> 30
                                    })
                                }
                            )
                        }
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Wifi,
                        title = stringResource(R.string.settings_auto_run_wifi),
                        trailing = {
                            Switch(
                                checked = state.autoRunOnWifi,
                                onCheckedChange = { viewModel.toggleAutoRunOnWifi(it) },
                                colors = customSwitchColors()
                            )
                        },
                        showDivider = false
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // PRO CARD
            ProUpgradeCard(
                isPro = state.isPro,
                onUpgradeClick = { viewModel.onUpgradeTapped() },
                onRestoreClick = { viewModel.onRestorePurchase() }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ABOUT SECTION
            SectionLabel(stringResource(R.string.settings_section_about))
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Outlined.StarOutline,
                        title = stringResource(R.string.settings_rate_app),
                        trailing = { ChevronIcon() },
                        onClick = { viewModel.onRateApp(context) }
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Share,
                        title = stringResource(R.string.settings_share_app),
                        trailing = { ChevronIcon() },
                        onClick = { viewModel.onShareApp(context) }
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Shield,
                        title = stringResource(R.string.settings_privacy_policy),
                        trailing = { ChevronIcon() },
                        onClick = { viewModel.onPrivacyPolicy(context) },
                        showDivider = false
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.settings_version, state.appVersion),
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        color = TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 0.dp, bottom = 8.dp)
    )
}

@Composable
fun ChevronIcon() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = TextSecondary,
        modifier = Modifier.size(14.dp)
    )
}

@Composable
fun customSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = PrimaryAccent,
    uncheckedThumbColor = Color.White,
    uncheckedTrackColor = Color(0xFF2E3A50),
    uncheckedBorderColor = Color.Transparent
)

@Composable
fun SettingsBottomNavigationBar(onNavigateToHome: () -> Unit, onNavigateToHistory: () -> Unit) {
    NavigationBar(
        containerColor = Background,
        tonalElevation = 0.dp,
        modifier = Modifier.border(0.5.dp, CardBorder, RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToHome,
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryAccent,
                unselectedIconColor = TextSecondary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToHistory,
            icon = { Icon(Icons.Default.History, contentDescription = "History") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryAccent,
                unselectedIconColor = TextSecondary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = true,
            onClick = { },
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

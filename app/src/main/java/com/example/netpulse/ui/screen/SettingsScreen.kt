package com.example.netpulse.ui.screen

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.netpulse.R
import com.example.netpulse.navigation.NavRoutes
import com.example.netpulse.ui.components.AppBottomNavigation
import com.example.netpulse.ui.components.ProUpgradeCard
import com.example.netpulse.ui.components.SegmentedControl
import com.example.netpulse.ui.components.SettingsRow
import com.example.netpulse.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showPermissionRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setNotificationsEnabled(true)
        } else {
            viewModel.setNotificationsEnabled(false)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AppBottomNavigation(
                currentRoute = NavRoutes.Settings,
                onNavigateToHome = onNavigateToHome,
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToSettings = { /* Already here */ }
            )
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
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // GENERAL SECTION
            SectionLabel(stringResource(R.string.settings_section_general))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                onCheckedChange = { enabled ->
                                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        val status = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                                        if (status != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                            showPermissionRationale = true
                                        } else {
                                            viewModel.toggleNotifications(true)
                                        }
                                    } else {
                                        viewModel.toggleNotifications(enabled)
                                    }
                                },
                                colors = customSwitchColors()
                            )
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // TEST SETTINGS SECTION
            SectionLabel(stringResource(R.string.settings_section_test))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
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
            
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // PRO CARD
//            ProUpgradeCard(
//                isPro = state.isPro,
//                onUpgradeClick = { viewModel.onUpgradeTapped() },
//                onRestoreClick = { viewModel.onRestorePurchase() }
//            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ABOUT SECTION
            SectionLabel(stringResource(R.string.settings_section_about))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
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
                        onClick = onNavigateToPrivacyPolicy,
                        showDivider = false
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.settings_version, state.appVersion),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Allow Notifications") },
            text = { Text("NetPulse will notify you when speed tests complete and alert you about speed drops.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionRationale = false
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Not Now")
                }
            }
        )
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(14.dp)
    )
}

@Composable
fun customSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = MaterialTheme.colorScheme.primary,
    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
    uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    uncheckedBorderColor = MaterialTheme.colorScheme.outline
)

@Composable
fun SettingsBottomNavigationBar(onNavigateToHome: () -> Unit, onNavigateToHistory: () -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToHome,
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToHistory,
            icon = { Icon(Icons.Default.History, contentDescription = "History") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text(stringResource(R.string.screen_settings)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )
    }
}

package com.example.netpulse.ui.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.data.Prefs
import com.example.netpulse.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateToHome: () -> Unit, onNavigateToHistory: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context.applicationContext) }
    var darkMode by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        prefs.darkTheme.collect { darkMode = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        bottomBar = {
            SettingsBottomNavigationBar(onNavigateToHome, onNavigateToHistory)
        },
        containerColor = Background
    ) { paddingValues ->
        SettingsScreenContent(
            darkMode = darkMode,
            onDarkModeChange = { enabled ->
                darkMode = enabled
                scope.launch { prefs.setDarkTheme(enabled) }
            },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

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
            label = { Text("Settings") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryAccent,
                selectedTextColor = PrimaryAccent,
                unselectedIconColor = TextSecondary,
                indicatorColor = Color.Transparent
            )
        )
    }
}

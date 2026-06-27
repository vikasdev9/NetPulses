package com.example.netpulse.ui.screen.wifi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.netpulse.ui.components.SettingsRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiScannerSettingsScreen(
    onBack: () -> Unit
) {
    var masterEnable by remember { mutableStateOf(true) }
    var autoScan by remember { mutableStateOf(true) }
    var showDistance by remember { mutableStateOf(true) }
    var showSecurityRating by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wi-Fi Scanner Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Text(
                    "GENERAL",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                SettingsRow(
                    icon = Icons.Default.PowerSettingsNew,
                    title = "Enable Nearby Wi-Fi Scanner",
                    subtitle = "Allow scanning for nearby networks",
                    trailing = {
                        Switch(checked = masterEnable, onCheckedChange = { masterEnable = it })
                    }
                )
                
                SettingsRow(
                    icon = Icons.Default.AutoMode,
                    title = "Auto Scan",
                    subtitle = "Automatically refresh scan results",
                    trailing = {
                        Switch(checked = autoScan, onCheckedChange = { autoScan = it })
                    }
                )
            }
            
            item {
                Text(
                    "DISPLAY OPTIONS",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                SettingsRow(
                    icon = Icons.Default.Straighten,
                    title = "Show Estimated Distance",
                    subtitle = "Display approximate distance to router",
                    trailing = {
                        Switch(checked = showDistance, onCheckedChange = { showDistance = it })
                    }
                )
                
                SettingsRow(
                    icon = Icons.Default.Security,
                    title = "Show Security Rating",
                    subtitle = "Analyze network safety levels",
                    trailing = {
                        Switch(checked = showSecurityRating, onCheckedChange = { showSecurityRating = it })
                    }
                )
            }
        }
    }
}

package com.example.netpulse.ui.screen.lan

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.data.lan.*
import com.example.netpulse.ui.components.lan.LanDashboardHeader
import com.example.netpulse.ui.components.lan.LanDeviceCard
import com.example.netpulse.ui.viewmodel.LanScannerViewModel
import com.example.netpulse.ui.theme.AmberAccentIcon
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanScannerScreen(
    onBack: () -> Unit,
    viewModel: LanScannerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var showPermissionDenied by remember { mutableStateOf(false) }
    
    val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                     permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.startScan()
        } else {
            showPermissionDenied = true
        }
    }

    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    
    var deviceToRename by remember { mutableStateOf<LanDevice?>(null) }
    var deviceToAddNote by remember { mutableStateOf<LanDevice?>(null) }
    var deviceToViewHistory by remember { mutableStateOf<LanDevice?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scanner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, "Sort")
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            LanSortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.name) },
                                    onClick = { viewModel.setSortOption(option); showSortMenu = false },
                                    leadingIcon = { if (uiState.sortOption == option) Icon(Icons.Default.Check, null) }
                                )
                            }
                        }
                    }
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                        DropdownMenu(expanded = showFilterMenu, onDismissRequest = { showFilterMenu = false }) {
                            LanFilterOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.name) },
                                    onClick = { viewModel.setFilterOption(option); showFilterMenu = false },
                                    leadingIcon = { if (uiState.filterOption == option) Icon(Icons.Default.Check, null) }
                                )
                            }
                        }
                    }
                    IconButton(onClick = { showExportMenu = true }) {
                        Icon(Icons.Default.Share, "Export")
                        DropdownMenu(expanded = showExportMenu, onDismissRequest = { showExportMenu = false }) {
                            listOf("PDF", "CSV", "JSON").forEach { format ->
                                DropdownMenuItem(
                                    text = { Text("Export as $format") },
                                    onClick = { 
                                        val file = viewModel.exportResults(context, format)
                                        if (file != null) {
                                            // Share intent
                                            val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                type = "application/*"
                                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(android.content.Intent.createChooser(intent, "Share Report"))
                                        }
                                        showExportMenu = false 
                                    },
                                    leadingIcon = { Icon(Icons.Default.FileDownload, null) }
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = { 
                            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.ACCESS_FINE_LOCATION
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            
                            if (hasPermission) viewModel.startScan()
                            else locationPermissionLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION))
                        },
                        enabled = !uiState.isScanning
                    ) {
                        if (uiState.isScanning) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, "Scan")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search Hostname, Nickname or IP") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = { if (uiState.searchQuery.isNotEmpty()) IconButton(onClick = { viewModel.updateSearchQuery("") }) { Icon(Icons.Default.Close, null) } },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    LanDashboardHeader(uiState = uiState)
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DEVICES (${uiState.devices.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        if (uiState.filterOption != LanFilterOption.ALL) {
                            AssistChip(
                                onClick = { viewModel.setFilterOption(LanFilterOption.ALL) },
                                label = { Text(uiState.filterOption.name) },
                                trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (uiState.devices.isEmpty()) {
                    item {
                        EmptyState(uiState.isScanning, uiState.searchQuery.isNotEmpty())
                    }
                }

                items(uiState.devices, key = { it.ipAddress }) { device ->
                    LanDeviceCard(
                        device = device,
                        onFavoriteToggle = { viewModel.toggleFavorite(device) },
                        onRename = { deviceToRename = device },
                        onAddNote = { deviceToAddNote = device },
                        onViewHistory = { deviceToViewHistory = device }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }

    // Rename Dialog
    deviceToRename?.let { device ->
        var name by remember { mutableStateOf(device.nickname ?: "") }
        AlertDialog(
            onDismissRequest = { deviceToRename = null },
            title = { Text("Rename Device") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nickname") },
                    placeholder = { Text(device.hostname) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.updateNickname(device, if (name.isBlank()) null else name)
                    deviceToRename = null 
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { deviceToRename = null }) { Text("Cancel") }
            }
        )
    }

    // Note Dialog
    deviceToAddNote?.let { device ->
        var notes by remember { mutableStateOf(device.notes ?: "") }
        AlertDialog(
            onDismissRequest = { deviceToAddNote = null },
            title = { Text("Device Notes") },
            text = {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.height(120.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.updateNotes(device, if (notes.isBlank()) null else notes)
                    deviceToAddNote = null 
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { deviceToAddNote = null }) { Text("Cancel") }
            }
        )
    }

    // History Dialog
    deviceToViewHistory?.let { device ->
        AlertDialog(
            onDismissRequest = { deviceToViewHistory = null },
            title = { Text("Connection History") },
            text = {
                Column {
                    DetailRow("First Seen", SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(device.firstSeen)))
                    DetailRow("Last Seen", SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(device.lastSeen)))
                    DetailRow("IP Address", device.ipAddress)
                    DetailRow("MAC Address", device.macAddress)
                }
            },
            confirmButton = {
                TextButton(onClick = { deviceToViewHistory = null }) { Text("Close") }
              }
        )
    }

    if (showPermissionDenied) {
        AlertDialog(
            onDismissRequest = { showPermissionDenied = false },
            title = { Text("Location Permission Required") },
            text = { Text("NetPulse needs location access to scan and identify devices on your local network.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDenied = false
                    locationPermissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }) { Text("Grant") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDenied = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EmptyState(isScanning: Boolean, isSearching: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (isSearching) Icons.Default.SearchOff else Icons.Default.DevicesOther,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isScanning) "Discovery in progress..." 
                       else if (isSearching) "No devices match your search"
                       else "No devices found. Ensure Wi-Fi is connected.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

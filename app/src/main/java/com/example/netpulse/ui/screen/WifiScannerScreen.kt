package com.example.netpulse.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.netpulse.data.wifi.WifiNetwork
import com.example.netpulse.navigation.NavRoutes
import com.example.netpulse.ui.components.wifi.ChannelAnalyzerCard
import com.example.netpulse.ui.components.wifi.SecurityAnalyzerCard
import com.example.netpulse.ui.components.wifi.WifiNetworkCard
import com.example.netpulse.ui.components.wifi.WifiSummaryCard
import com.example.netpulse.ui.theme.AmberAccentIcon
import com.example.netpulse.ui.viewmodel.WifiScannerViewModel
import com.example.netpulse.utils.wifi.ScanState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiScannerScreen(
    navController: NavController,
    viewModel: WifiScannerViewModel
) {
    val scanResults by viewModel.scanResults.collectAsState()
    val scanState by viewModel.scanState.collectAsState()
    val context = LocalContext.current
    var showPermissionDenied by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                     permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.startScan()
        } else {
            showPermissionDenied = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("WiFi Scanner", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(NavRoutes.WIFI_SCANNER_SETTINGS) }) {
                        Icon(Icons.Default.Settings, "Settings", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = scanState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                },
                label = "ScanStateTransition"
            ) { targetState ->
                when (targetState) {
                    is ScanState.Idle, is ScanState.Scanning -> {
                        RadarScanView(
                            isScanning = targetState is ScanState.Scanning,
                            scanResults = scanResults,
                            onStartScan = {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    viewModel.startScan()
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            }
                        )
                    }
                    is ScanState.Complete, is ScanState.Error -> {
                        ResultsStateContent(
                            scanResults = scanResults,
                            onScanAgain = { viewModel.startScan() }
                        )
                    }
                }
            }

            if (showPermissionDenied) {
                PermissionDeniedDialog(
                    onGrantClick = {
                        showPermissionDenied = false
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun RadarScanView(
    isScanning: Boolean,
    scanResults: List<WifiNetwork>,
    onStartScan: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Radar")
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Center Radar
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Static concentric circles (Optimized with drawBehind)
                val circleColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                Spacer(
                    modifier = Modifier
                        .size(300.dp)
                        .drawBehind {
                            val radius = size.minDimension / 2
                            drawCircle(color = circleColor, radius = radius * 0.3f, style = Stroke(1.dp.toPx()))
                            drawCircle(color = circleColor, radius = radius * 0.6f, style = Stroke(1.dp.toPx()))
                            drawCircle(color = circleColor, radius = radius, style = Stroke(1.dp.toPx()))
                        }
                )

                // Ripples (Optimized with graphicsLayer)
                if (isScanning) {
                    val rippleColor = MaterialTheme.colorScheme.primary
                    repeat(3) { index ->
                        val rippleScale by infiniteTransition.animateFloat(
                            initialValue = 0.2f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2500, delayMillis = index * 800, easing = LinearOutSlowInEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "Ripple$index"
                        )
                        val rippleAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 0.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2500, delayMillis = index * 800, easing = LinearOutSlowInEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "Alpha$index"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(300.dp)
                                .graphicsLayer {
                                    scaleX = rippleScale
                                    scaleY = rippleScale
                                    alpha = rippleAlpha
                                }
                                .drawBehind {
                                    drawCircle(color = rippleColor)
                                }
                        )
                    }
                    
                    // Sweeping line effect for more realism
                    val sweepAngle by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(4000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "Sweep"
                    )
                    
                    Canvas(modifier = Modifier.size(300.dp)) {
                        val radius = size.minDimension / 2
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(Color.Transparent, rippleColor.copy(alpha = 0.3f), Color.Transparent),
                                center = center
                            ),
                            startAngle = sweepAngle - 30f,
                            sweepAngle = 30f,
                            useCenter = true
                        )
                        
                        val angleRad = Math.toRadians(sweepAngle.toDouble())
                        drawLine(
                            color = rippleColor.copy(alpha = 0.5f),
                            start = center,
                            end = Offset(
                                (center.x + radius * Math.cos(angleRad)).toFloat(),
                                (center.y + radius * Math.sin(angleRad)).toFloat()
                            ),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }

                // Discovered Nodes
                scanResults.forEachIndexed { index, network ->
                    key(network.bssid) {
                        RadarNode(
                            network = network,
                            index = index,
                            total = 8 // We only show up to 8 nodes on radar for clarity
                        )
                    }
                }

                // Central Avatar
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(80.dp)
                        .border(4.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
                        .shadow(12.dp, CircleShape),
                    tonalElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Wifi,
                            null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (isScanning) "Searching nearby..." else "Ready to Discover",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // Bottom Section
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isScanning) "Analyzing spectrum..." else "Scan for available networks",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(20.dp))

                Box(contentAlignment = Alignment.Center) {
                   Surface(
                       onClick = onStartScan,
                       shape = CircleShape,
                       color = if (isScanning) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                       modifier = Modifier
                           .size(64.dp)
                           .shadow(8.dp, CircleShape),
                       enabled = !isScanning
                   ) {
                       Box(contentAlignment = Alignment.Center) {
                           if (isScanning) {
                               CircularProgressIndicator(
                                   modifier = Modifier.size(32.dp),
                                   color = MaterialTheme.colorScheme.primary,
                                   strokeWidth = 3.dp
                               )
                           } else {
                               Icon(
                                   Icons.Default.Refresh,
                                   contentDescription = null,
                                   tint = MaterialTheme.colorScheme.onPrimary,
                                   modifier = Modifier.size(32.dp)
                               )
                           }
                       }
                   }
                }
            }
        }
    }
}

@Composable
fun RadarNode(
    network: WifiNetwork,
    index: Int,
    total: Int
) {
    val angle = (index * (360f / total)).toDouble()
    val radius = 110.dp // Radius for orbit
    
    val infiniteTransition = rememberInfiniteTransition(label = "Node$index")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500 + (index * 100), easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Float"
    )

    // Calculate position
    val x = (radius.value * Math.cos(Math.toRadians(angle))).toFloat()
    val y = (radius.value * Math.sin(Math.toRadians(angle))).toFloat()

    val appearScale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        appearScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationX = x.dp.toPx()
                translationY = (y.dp + floatAnim.dp).toPx()
                scaleX = appearScale.value
                scaleY = appearScale.value
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = when (index % 3) {
                    0 -> MaterialTheme.colorScheme.primary
                    1 -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.tertiary
                },
                modifier = Modifier
                    .size(48.dp)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .shadow(4.dp, CircleShape)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (network.ssid.isNotEmpty()) network.ssid.take(1).uppercase() else "?",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (network.isHidden) "Hidden" else network.ssid.take(8),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(60.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ResultsStateContent(
    scanResults: List<WifiNetwork>,
    onScanAgain: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                WifiSummaryCard(networks = scanResults)
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ChannelAnalyzerCard(networks = scanResults)
                    SecurityAnalyzerCard(networks = scanResults)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "DISCOVERED NETWORKS (${scanResults.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            items(scanResults, key = { it.bssid }) { network ->
                val index = scanResults.indexOf(network)
                val animatedAlpha = remember { Animatable(0f) }
                val animatedOffset = remember { Animatable(50f) }
                
                LaunchedEffect(network.bssid) {
                    delay(index.coerceAtMost(10) * 50L)
                    launch { animatedAlpha.animateTo(1f, tween(400)) }
                    launch { animatedOffset.animateTo(0f, tween(400)) }
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            alpha = animatedAlpha.value
                            translationY = animatedOffset.value
                        }
                ) {
                    WifiNetworkCard(network = network)
                }
            }
        }

        // Floating Action Button style for Scan Again
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                        startY = 0f,
                        endY = 100f
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            ActionButton(text = "Scan Again", icon = Icons.Default.Refresh, onClick = onScanAgain)
        }
    }
}

@Composable
fun ActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text, color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun PermissionDeniedDialog(onGrantClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Outlined.Info, null, modifier = Modifier.size(48.dp), tint = AmberAccentIcon)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Location Permission Required", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Android requires location access to scan nearby WiFi networks and show network names. Please grant permission to continue.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onGrantClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

package com.example.netpulse.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.netpulse.R
import com.example.netpulse.utils.WidgetPinningManager
import com.example.netpulse.widget.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetCollectionScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    fun requestPin(receiver: Class<*>) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            WidgetPinningManager.pinWidget(context, receiver)
        } else {
            Toast.makeText(context, "Direct pinning not supported on this Android version. Please add from home screen.", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Widget Collection", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Choose a widget to add to your home screen. Press and hold on your home screen to add them.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            WidgetPreviewCard(
                name = "Responsive Speed Badge",
                description = "Smart widget that adjusts its layout based on size. Shows download, upload and ping.",
                size = "2x2 to 4x2",
                onAddClick = {
                    requestPin(NetPulseWidgetReceiver::class.java)
                },
                previewContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color(0xFFF0F4FF), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF3B8BFF))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("NetPulse", color = Color(0xFF0A0E1A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("94.2 Mbps", color = Color(0xFF3B8BFF), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }
                }
            )

            WidgetPreviewCard(
                name = "Quick Speed Test",
                description = stringResource(R.string.widget_quick_test_desc),
                size = "4x2",
                onAddClick = {
                    requestPin(QuickSpeedTestWidgetReceiver::class.java)
                },
                previewContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color(0xFF0A0E1A), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("NetPulse", color = Color(0xFF00D4FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(Modifier.weight(1f))
                                Text("WiFi", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("94.2", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(4.dp))
                                Text("Mbps", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                WidgetMiniStat("↓ 94.2")
                                WidgetMiniStat("↑ 48.5")
                                WidgetMiniStat("⬤ 18ms")
                            }
                        }
                    }
                }
            )

            WidgetPreviewCard(
                name = "Live Speed Badge",
                description = stringResource(R.string.widget_live_speed_desc),
                size = "2x1",
                onAddClick = {
                    requestPin(LiveSpeedWidgetReceiver::class.java)
                },
                previewContent = {
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .height(60.dp)
                            .background(Color(0xFF0A0E1A), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(0xFF3B8BFF), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("N", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("94.2 Mbps", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Ping 18ms", color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                            }
                        }
                    }
                }
            )

            WidgetPreviewCard(
                name = "Network Intelligence",
                description = stringResource(R.string.widget_intelligence_desc),
                size = "4x2",
                onAddClick = {
                    requestPin(InternetIntelligenceWidgetReceiver::class.java)
                },
                previewContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color(0xFFF0F4FF), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFF3B8BFF).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("NETWORK STATUS", color = Color(0xFF3B8BFF), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("Jio Fiber 5G", color = Color(0xFF0A0E1A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("IP: 192.168.1.1 · ISP: Reliance Jio", color = Color(0xFF475569), fontSize = 11.sp)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(8.dp).background(Color(0xFF00E676), RoundedCornerShape(4.dp)))
                                Spacer(Modifier.width(6.dp))
                                Text("Connection Secure", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            )

            WidgetPreviewCard(
                name = "Performance Dashboard",
                description = stringResource(R.string.widget_dashboard_desc),
                size = "4x2",
                onAddClick = {
                    requestPin(PerformanceDashboardWidgetReceiver::class.java)
                },
                previewContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(Color(0xFFF0F4FF), RoundedCornerShape(20.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("PERFORMANCE", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            Spacer(Modifier.height(4.dp))
                            Row {
                                Column(Modifier.weight(1f)) {
                                    Text("94.2", color = Color(0xFF0A0E1A), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                    Text("Mbps Down", color = Color(0xFF475569), fontSize = 9.sp)
                                }
                                Column(Modifier.weight(1f)) {
                                    Text("48.5", color = Color(0xFF0A0E1A), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                    Text("Mbps Up", color = Color(0xFF475569), fontSize = 9.sp)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Gaming: Excellent", color = Color(0xFF2563EB), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("Ping: 18ms", color = Color(0xFF0A0E1A), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            )

            WidgetPreviewCard(
                name = "Mini Analytics",
                description = stringResource(R.string.widget_mini_desc),
                size = "2x2",
                onAddClick = {
                    requestPin(MiniAnalyticsWidgetReceiver::class.java)
                },
                previewContent = {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF3B8BFF), Color(0xFF00D4FF))),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("98", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                            Text("SCORE", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun WidgetPreviewCard(
    name: String,
    description: String,
    size: String,
    onAddClick: () -> Unit,
    previewContent: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = size,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                previewContent()
            }
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add to Home Screen")
            }
        }
    }
}

@Composable
fun WidgetMiniStat(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.1f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

package com.example.netpulse.ui.screen

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.netpulse.data.SpeedResult
import com.example.netpulse.ui.components.HistoryCard
import com.example.netpulse.ui.theme.*
import com.example.netpulse.ui.viewmodel.HistoryViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val results by viewModel.allResults.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    
    // Bottom Sheet State
    var showSheet by remember { mutableStateOf(false) }
    var selectedResult by remember { mutableStateOf<SpeedResult?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val filteredResults = if (selectedFilter == "All") {
        results
    } else {
        results.filter { it.networkType == selectedFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History", color = Color.White) },
                actions = {
                    IconButton(onClick = { viewModel.clearAll() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkColor)
            )
        },
        containerColor = DarkColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DarkGradient)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "WiFi", "5G", "4G").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Teal200,
                            selectedLabelColor = Color.Black,
                            labelColor = Color.White
                        )
                    )
                }
            }

            if (filteredResults.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No results found", color = Color.White.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredResults, key = { it.id }) { result ->
                        HistoryCard(
                            result = result,
                            dateLabel = formatDateLabel(result.timestamp),
                            onDelete = { viewModel.deleteResult(result.id) },
                            onShare = { 
                                selectedResult = result
                                showSheet = true
                            }
                        )
                    }
                }
            }
        }

        // Share Bottom Sheet
        if (showSheet && selectedResult != null) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = CardSurface,
                dragHandle = { BottomSheetDefaults.DragHandle(color = CardBorder) }
            ) {
                ShareSheetContent(
                    result = selectedResult!!,
                    onDismiss = { showSheet = false }
                )
            }
        }
    }
}

@Composable
fun ShareSheetContent(result: SpeedResult, onDismiss: () -> Unit) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Share Result",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(8.dp))
        
        Text(
            "Select how you want to share your test",
            color = TextSecondary,
            fontSize = 14.sp
        )
        
        Spacer(Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ShareOptionButton(
                icon = Icons.Outlined.Share,
                title = "Share as Text",
                subtitle = "Copy metrics as text",
                modifier = Modifier.weight(1f),
                onClick = {
                    shareResult(context, result)
                    onDismiss()
                }
            )
            
            ShareOptionButton(
                icon = Icons.Outlined.Image,
                title = "Share Image",
                subtitle = "Share as Card",
                modifier = Modifier.weight(1f),
                onClick = {
                    shareResultImage(context, result)
                    onDismiss()
                }
            )
        }

        Spacer(Modifier.height(32.dp))

        // Metrics Summary Preview
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Download", color = TextSecondary, fontSize = 12.sp)
                    Text("Upload", color = TextSecondary, fontSize = 12.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("%.1f Mbps".format(result.downloadMbps), color = Color(0xFF3B8BFF), fontWeight = FontWeight.Bold)
                    Text("%.1f Mbps".format(result.uploadMbps), color = Color(0xFF00D4FF), fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        TextButton(onClick = onDismiss) {
            Text("Cancel", color = TextSecondary)
        }
    }
}

@Composable
fun ShareOptionButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(130.dp) // Increased from 120dp to prevent cutting
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = CardSurface,
        border = BorderStroke(1.dp, CardBorder),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp), // Slightly reduced padding to give more room
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryAccent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PrimaryAccent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, color = TextSecondary, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

fun shareResultImage(context: Context, result: SpeedResult) {
    val bitmap = Bitmap.createBitmap(1080, 1350, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // 1. Background (Dark Gradient Style)
    val paint = Paint().apply { isAntiAlias = true }
    paint.color = android.graphics.Color.parseColor("#0F1729")
    canvas.drawRect(0f, 0f, 1080f, 1350f, paint)
    
    // 2. Decorative elements (Arc Glow)
    paint.color = android.graphics.Color.parseColor("#1A2744")
    canvas.drawCircle(540f, 400f, 600f, paint)
    
    // 3. Logo/App Name
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 48f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("NetPulse", 80f, 120f, paint)
    
    // 4. Main Speed (Download)
    paint.textAlign = Paint.Align.CENTER
    paint.color = android.graphics.Color.parseColor("#3B8BFF")
    paint.textSize = 180f
    canvas.drawText("%.1f".format(result.downloadMbps), 540f, 500f, paint)
    
    paint.textSize = 40f
    paint.color = android.graphics.Color.parseColor("#94A3B8")
    canvas.drawText("Mbps Download", 540f, 560f, paint)
    
    // 5. Secondary Speed (Upload)
    paint.color = android.graphics.Color.parseColor("#00D4FF")
    paint.textSize = 120f
    canvas.drawText("%.1f".format(result.uploadMbps), 540f, 750f, paint)
    
    paint.textSize = 36f
    paint.color = android.graphics.Color.parseColor("#94A3B8")
    canvas.drawText("Mbps Upload", 540f, 800f, paint)
    
    // 6. Stats Grid
    val gridTop = 950f
    paint.color = android.graphics.Color.parseColor("#1E293B")
    val rect = RectF(100f, gridTop, 980f, gridTop + 240f)
    canvas.drawRoundRect(rect, 30f, 30f, paint)
    
    paint.textAlign = Paint.Align.LEFT
    paint.textSize = 32f
    paint.color = android.graphics.Color.parseColor("#64748B")
    canvas.drawText("Ping", 180f, gridTop + 80f, paint)
    canvas.drawText("Jitter", 580f, gridTop + 80f, paint)
    canvas.drawText("Network", 180f, gridTop + 180f, paint)
    canvas.drawText("ISP", 580f, gridTop + 180f, paint)
    
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 42f
    canvas.drawText("${result.pingMs} ms", 180f, gridTop + 130f, paint)
    canvas.drawText("${result.jitterMs} ms", 580f, gridTop + 130f, paint)
    canvas.drawText(result.networkType, 180f, gridTop + 230f, paint)
    canvas.drawText(result.isp, 580f, gridTop + 230f, paint)
    
    // 7. Footer
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = 30f
    paint.color = android.graphics.Color.parseColor("#475569")
    canvas.drawText("Tested with NetPulse Speed Test App", 540f, 1300f, paint)

    // Save and Share
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "netpulse_result_${result.id}.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()
        
        val contentUri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Result Card"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun shareResult(context: Context, result: SpeedResult) {
    val shareBody = """
        🚀 NetPulse Speed Test Result
        ----------------------------
        Date: ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(result.timestamp))}
        Network: ${result.networkType} (${result.isp})
        
        ⬇️ Download: ${"%.1f".format(result.downloadMbps)} Mbps
        ⬆️ Upload: ${"%.1f".format(result.uploadMbps)} Mbps
        📶 Ping: ${result.pingMs} ms
        〰️ Jitter: ${result.jitterMs} ms
        
        Tested with NetPulse App
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "NetPulse Speed Test Result")
        putExtra(Intent.EXTRA_TEXT, shareBody)
    }
    context.startActivity(Intent.createChooser(intent, "Share Result via"))
}

@Composable
fun DetailItem(label: String, value: String, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.labelMedium)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = color, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(4.dp))
            Text(unit, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        }
    }
}

fun formatDateLabel(timestamp: Long): String {
    val now = Calendar.getInstance()
    val resultCal = Calendar.getInstance().apply { timeInMillis = timestamp }

    return when {
        isSameDay(now, resultCal) -> "Today, " + formatTime(timestamp)
        isYesterday(now, resultCal) -> "Yesterday, " + formatTime(timestamp)
        else -> java.text.SimpleDateFormat("EEE, h:mm a", Locale.getDefault()).format(Date(timestamp))
    }
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun isYesterday(now: Calendar, then: Calendar): Boolean {
    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return yesterday.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
}

fun formatTime(timestamp: Long): String {
    return java.text.SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
}

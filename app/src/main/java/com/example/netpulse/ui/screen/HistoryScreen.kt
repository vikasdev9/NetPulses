package com.example.netpulse.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.netpulse.data.SpeedResult
import com.example.netpulse.ui.components.HistoryCard
import com.example.netpulse.ui.components.ResultShareBottomSheet
import com.example.netpulse.ui.theme.*
import com.example.netpulse.ui.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val historyItems by viewModel.historyItems.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var selectedItemForShare by remember { mutableStateOf<SpeedResult?>(null) }

    val filteredItems = remember(historyItems, selectedFilter) {
        if (selectedFilter == "All") historyItems
        else historyItems.filter { it.networkType == selectedFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Test History",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    TextButton(onClick = { viewModel.clearAll() }) {
                        Text(
                            "Clear All",
                            color = PrimaryAccent,
                            fontSize = 14.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            HistoryBottomNavigationBar(onNavigateToHome, onNavigateToSettings)
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            FilterChipsRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { viewModel.setFilter(it) }
            )

            if (filteredItems.isEmpty()) {
                EmptyHistoryState()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteItem(item)
                                    true
                                } else false
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color by animateColorAsState(
                                    when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.EndToStart -> Color.Red
                                        else -> Color.Transparent
                                    }, label = "deleteBackground"
                                )
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White
                                    )
                                }
                            },
                            enableDismissFromStartToEnd = false,
                            content = {
                                HistoryCard(
                                    result = item,
                                    onDelete = { viewModel.deleteItem(item) },
                                    onShare = { selectedItemForShare = item }
                                )
                            }
                        )
                    }
                }
            }
        }

        selectedItemForShare?.let { item ->
            ResultShareBottomSheet(
                result = item,
                onDismiss = { selectedItemForShare = null },
                isPro = false // Default to false, can be connected to actual pro status
            )
        }
    }
}

@Composable
fun FilterChipsRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf("All", "WiFi", "5G", "4G", "This Week", "This Month")
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(filters) { filter ->
            val isActive = selectedFilter == filter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(if (isActive) PrimaryAccent else CardSurface)
                    .then(
                        if (!isActive) Modifier.border(1.dp, CardBorder, RoundedCornerShape(99.dp))
                        else Modifier
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = filter,
                    color = if (isActive) Color.White else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EmptyHistoryState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "No test history found",
            color = TextSecondary,
            fontSize = 16.sp
        )
    }
}

@Composable
fun HistoryBottomNavigationBar(onNavigateToHome: () -> Unit, onNavigateToSettings: () -> Unit) {
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
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.History, contentDescription = "History") },
            label = { Text("History") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryAccent,
                selectedTextColor = PrimaryAccent,
                unselectedIconColor = TextSecondary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToSettings,
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryAccent,
                unselectedIconColor = TextSecondary,
                indicatorColor = Color.Transparent
            )
        )
    }
}

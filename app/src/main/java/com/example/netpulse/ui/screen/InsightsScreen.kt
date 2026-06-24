package com.example.netpulse.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.netpulse.NetPulseApplication
import com.example.netpulse.data.datastore.UserPreferences
import com.example.netpulse.insights.achievements.*
import com.example.netpulse.insights.healthscore.*
import com.example.netpulse.insights.isp.*
import com.example.netpulse.insights.recommendations.*
import com.example.netpulse.insights.trends.*
import com.example.netpulse.insights.usage.*
import com.example.netpulse.insights.wifistability.*
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as NetPulseApplication
    val speedDao = app.database.speedResultDao()
    val ispDao = app.database.ispDao()
    val achievementDao = app.database.achievementDao()
    val wifiDao = app.database.wifiStabilityDao()
    val userPrefs = UserPreferences(context)

    // Manual ViewModel creation since Hilt setup failed in this environment
    val healthViewModel: HealthScoreViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = HealthScoreViewModel(speedDao) as T
    })
    
    val trendsViewModel: TrendsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = TrendsViewModel(speedDao) as T
    })
    
    val usageViewModel: UsageViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = UsageViewModel(userPrefs) as T
    })
    
    val ispViewModel: ISPViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = ISPViewModel(ispDao, userPrefs) as T
    })
    
    val achievementViewModel: AchievementViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = AchievementViewModel(achievementDao, speedDao) as T
    })
    
    val wifiViewModel: WifiStabilityViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = WifiStabilityViewModel(wifiDao) as T
    })
    
    val recsViewModel: RecommendationsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = RecommendationsViewModel(speedDao, userPrefs) as T
    })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network Insights", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { HealthScoreCard(healthViewModel) }
            item { RecommendationsList(recsViewModel) }
            item { UsageCard(usageViewModel) }
            item { TrendsCard(trendsViewModel) }
            item { ISPCard(ispViewModel) }
            item { WifiStabilityCard(wifiViewModel) }
            item { AchievementGrid(achievementViewModel) }
            
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

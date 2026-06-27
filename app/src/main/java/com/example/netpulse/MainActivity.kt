package com.example.netpulse

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.netpulse.data.datastore.UserPreferences
import com.example.netpulse.navigation.NavRoutes
import com.example.netpulse.ui.screen.*
import com.example.netpulse.ui.theme.NetPulseTheme
import com.example.netpulse.ui.viewmodel.*
import com.example.netpulse.utils.LocaleUtils
import com.example.netpulse.utils.WiFiAutoRunManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private lateinit var userPreferences: UserPreferences
    private lateinit var wifiAutoRunManager: WiFiAutoRunManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPreferences = UserPreferences(applicationContext)

        val settingsViewModel: SettingsViewModel by viewModels {
            object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(application, userPreferences) as T
                }
            }
        }

        val speedTestViewModel: SpeedTestViewModel by viewModels {
            SpeedTestViewModel.Factory(application, userPreferences)
        }

        val speedTestSettingsViewModel: SpeedTestSettingsViewModel by viewModels {
            SpeedTestSettingsViewModel.Factory(application, userPreferences)
        }

        val analyticsViewModel: AnalyticsViewModel by viewModels()
        val historyViewModel: HistoryViewModel by viewModels()

        wifiAutoRunManager = WiFiAutoRunManager(this) {
            lifecycleScope.launch {
                val autoRun = userPreferences.autoRunOnWifi.first()
                if (autoRun) {
                    delay(2000) // connection stabilizes
                    speedTestViewModel.startTest()
                }
            }
        }

        setContent {
            val settingsState by settingsViewModel.state.collectAsState()
            
            NetPulseTheme(darkTheme = settingsState.isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    var onboardingDone by remember { mutableStateOf<Boolean?>(null) }

                    LaunchedEffect(Unit) {
                        userPreferences.onboardingDone.collect { done ->
                            onboardingDone = done
                        }
                    }

                    if (onboardingDone == null) {
                        SplashScreen { }
                    } else {
                        val startTest = intent.getBooleanExtra("START_TEST", false)
                        val navigateTo = intent.getStringExtra("NAVIGATE_TO")
                        
                        LaunchedEffect(intent) {
                            if (startTest) {
                                speedTestViewModel.startTest()
                            }
                        }

                        NavHost(
                            navController = navController,
                            startDestination = if (navigateTo == "analytics") NavRoutes.Analytics else NavRoutes.Splash
                        ) {
                            composable(NavRoutes.Splash) {
                                SplashScreen {
                                    if (onboardingDone == true) {
                                        navController.navigate(NavRoutes.Home) {
                                            popUpTo(NavRoutes.Splash) { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate(NavRoutes.Onboarding) {
                                            popUpTo(NavRoutes.Splash) { inclusive = true }
                                        }
                                    }
                                }
                            }
                            composable(NavRoutes.Onboarding) {
                                OnboardingScreen {
                                    lifecycleScope.launch { userPreferences.setOnboardingDone(true) }
                                    navController.navigate(NavRoutes.Home) {
                                        popUpTo(NavRoutes.Onboarding) { inclusive = true }
                                    }
                                }
                            }
                            composable(NavRoutes.Home) {
                                MainScreen(
                                    onNavigateToHistory = {
                                        navController.navigate(NavRoutes.History) { launchSingleTop = true }
                                    },
                                    onNavigateToSettings = {
                                        navController.navigate(NavRoutes.Settings) { launchSingleTop = true }
                                    },
                                    onNavigateToAnalytics = {
                                        navController.navigate(NavRoutes.Analytics) { launchSingleTop = true }
                                    },
                                    viewModel = speedTestViewModel
                                )
                            }
                            composable(NavRoutes.Analytics) {
                                AnalyticsScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToDashboard = { navController.navigate(NavRoutes.AppUsageDashboard) },
                                    onNavigateToHistory = {
                                        navController.navigate(NavRoutes.History) { launchSingleTop = true }
                                    },
                                    onNavigateToSummary = { range ->
                                        navController.navigate("network_summary_detail/${range.name}")
                                    },
                                    viewModel = analyticsViewModel
                                )
                            }
                            composable(
                                route = NavRoutes.NetworkSummaryDetail,
                                arguments = listOf(androidx.navigation.navArgument("range") { type = androidx.navigation.NavType.StringType })
                            ) { backStackEntry ->
                                val rangeName = backStackEntry.arguments?.getString("range") ?: "TODAY"
                                val range = try { AnalyticsRange.valueOf(rangeName) } catch (e: Exception) { AnalyticsRange.TODAY }
                                NetworkSummaryDetailScreen(
                                    range = range,
                                    onBack = { navController.popBackStack() },
                                    viewModel = analyticsViewModel
                                )
                            }
                            composable(NavRoutes.AppUsageDashboard) {
                                AppUsageDashboardScreen(
                                    onBack = { navController.popBackStack() },
                                    viewModel = analyticsViewModel
                                )
                            }
                            composable(NavRoutes.History) {
                                HistoryScreen(
                                    onNavigateToHome = {
                                        navController.navigate(NavRoutes.Home) {
                                            popUpTo(NavRoutes.Home) { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    },
                                    onNavigateToSettings = {
                                        navController.navigate(NavRoutes.Settings) { launchSingleTop = true }
                                    },
                                    onBack = { navController.popBackStack() },
                                    viewModel = historyViewModel
                                )
                            }
                            composable(NavRoutes.Settings) {
                                SettingsScreen(
                                    onNavigateToHome = {
                                        navController.navigate(NavRoutes.Home) {
                                            popUpTo(NavRoutes.Home) { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    },
                                    onNavigateToHistory = {
                                        navController.navigate(NavRoutes.History) { launchSingleTop = true }
                                    },
                                    onNavigateToLanguage = { navController.navigate(NavRoutes.Language) },
                                    onNavigateToPrivacyPolicy = { navController.navigate(NavRoutes.PrivacyPolicy) },
                                    onNavigateToWidgetCollection = { navController.navigate(NavRoutes.WidgetCollection) },
                                    viewModel = settingsViewModel,
                                    speedTestViewModel = speedTestSettingsViewModel
                                )
                            }
                            composable(NavRoutes.Language) {
                                LanguageScreen(
                                    currentLanguageCode = settingsState.currentLanguageCode,
                                    onLanguageSelected = { lang ->
                                        // 1. Save synchronously for attachBaseContext on next run
                                        LocaleUtils.saveLanguage(applicationContext, lang.code)
                                        
                                        // 2. Save asynchronously to DataStore via ViewModel
                                        settingsViewModel.setLanguage(lang.code)
                                        
                                        // 3. Update current application context for immediate effect
                                        LocaleUtils.setLocale(applicationContext, lang.code)
                                        
                                        // 4. Force activity recreation to reload all string resources
                                        recreate()
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(NavRoutes.PrivacyPolicy) {
                                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
                            }
                            composable(NavRoutes.WidgetCollection) {
                                WidgetCollectionScreen(onBack = { navController.popBackStack() })
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            userPreferences.autoRunOnWifi.collect { enabled ->
                if (enabled) wifiAutoRunManager.startWatching()
                else wifiAutoRunManager.stopWatching()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        wifiAutoRunManager.stopWatching()
    }
}

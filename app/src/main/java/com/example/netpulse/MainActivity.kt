package com.example.netpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.example.netpulse.ui.viewmodel.AnalyticsViewModel
import com.example.netpulse.ui.viewmodel.SettingsViewModel
import com.example.netpulse.ui.viewmodel.SpeedTestViewModel
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
                    return SettingsViewModel(userPreferences) as T
                }
            }
        }

        val speedTestViewModel: SpeedTestViewModel by viewModels {
            SpeedTestViewModel.Factory(application, userPreferences)
        }

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
                        NavHost(
                            navController = navController,
                            startDestination = NavRoutes.Splash
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
                                    }
                                )
                            }
                            composable(NavRoutes.Analytics) {
                                AnalyticsScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToDashboard = { navController.navigate(NavRoutes.AppUsageDashboard) }
                                )
                            }
                            composable(NavRoutes.AppUsageDashboard) {
                                AppUsageDashboardScreen(
                                    onBack = { navController.popBackStack() },
                                    viewModel = androidx.lifecycle.viewmodel.compose.viewModel()
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
                                    onBack = { navController.popBackStack() }
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
                                    viewModel = settingsViewModel
                                )
                            }
                            composable(NavRoutes.Language) {
                                val currentLang = remember {
                                    mutableStateOf(LocaleUtils.getSavedLanguage(applicationContext).ifEmpty { "en" })
                                }
                                LanguageScreen(
                                    currentLanguageCode = currentLang.value,
                                    onLanguageSelected = { lang ->
                                        LocaleUtils.saveLanguage(applicationContext, lang.code)
                                        currentLang.value = lang.code
                                        recreate()
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(NavRoutes.PrivacyPolicy) {
                                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
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

package com.example.netpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.netpulse.data.Prefs
import com.example.netpulse.navigation.NavRoutes
import com.example.netpulse.ui.screen.HistoryScreen
import com.example.netpulse.ui.screen.MainScreen
import com.example.netpulse.ui.screen.OnboardingScreen
import com.example.netpulse.ui.screen.SettingsScreen
import com.example.netpulse.ui.screen.SplashScreen
import com.example.netpulse.ui.screen.AnalyticsScreen
import com.example.netpulse.ui.screen.LanguageScreen
import com.example.netpulse.ui.theme.NetPulseTheme
import com.example.netpulse.utils.LocaleUtils
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Prefs(applicationContext)

        setContent {
            var isDark by remember { mutableStateOf<Boolean?>(null) }
            LaunchedEffect(Unit) {
                prefs.darkTheme.collect { v -> isDark = v }
            }
            NetPulseTheme(darkTheme = (isDark ?: true)) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val navController = rememberNavController()
                    var onboardingDone by remember { mutableStateOf<Boolean?>(null) }

                    LaunchedEffect(Unit) {
                        prefs.onboardingDone.collect { done ->
                            onboardingDone = done
                        }
                    }

                    if (onboardingDone == null || isDark == null) {
                        // Simple splash while loading prefs
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
                                    lifecycleScope.launch { prefs.setOnboardingDone(true) }
                                    navController.navigate(NavRoutes.Home) {
                                        popUpTo(NavRoutes.Onboarding) { inclusive = true }
                                    }
                                }
                            }
                            composable(NavRoutes.Home) {
                                MainScreen(
                                    onNavigateToHistory = {
                                        navController.navigate(NavRoutes.History) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onNavigateToSettings = {
                                        navController.navigate(NavRoutes.Settings) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onNavigateToAnalytics = {
                                        navController.navigate(NavRoutes.Analytics) {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                            composable(NavRoutes.Analytics) {
                                AnalyticsScreen(
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
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
                                        navController.navigate(NavRoutes.Settings) {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                            try {
                                composable(NavRoutes.Settings) {
                                    SettingsScreen(
                                        onNavigateToHome = {
                                            navController.navigate(NavRoutes.Home) {
                                                popUpTo(NavRoutes.Home) { inclusive = false }
                                                launchSingleTop = true
                                            }
                                        },
                                        onNavigateToHistory = {
                                            navController.navigate(NavRoutes.History) {
                                                launchSingleTop = true
                                            }
                                        },
                                        onNavigateToLanguage = {
                                            navController.navigate(NavRoutes.Language)
                                        }
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
                                        onBack = {
                                            navController.popBackStack()
                                        }
                                    )
                                }
                            } catch (e: Exception) {
                                throw e
                            } finally {
                            }
                        }
                    }
                }
            }
        }
    }
}
package com.example.netpulse.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.netpulse.ui.screen.onboarding.*
import com.example.netpulse.ui.theme.DarkGradient
import com.example.netpulse.ui.theme.NetPulseTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = viewModel(),
    onFinished: () -> Unit
) {
    val items = remember { getOnboardingItems() }
    val pagerState = rememberPagerState(pageCount = { items.size })
    val scope = rememberCoroutineScope()
    val currentPage by viewModel.currentPage.collectAsState()

    // Sync ViewModel with PagerState
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onPageChanged(pagerState.currentPage)
    }

    // Handle Effects
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is OnboardingUiEffect.NavigateToHome -> onFinished()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkGradient)
                .padding(padding)
        ) {
            val isTablet = maxWidth > 600.dp
            val isLandscape = maxWidth > maxHeight
            val contentPadding = if (isTablet) 64.dp else 24.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // TOP BAR: Navigation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = contentPadding, vertical = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (currentPage < items.size - 1) {
                        TextButton(
                            onClick = { viewModel.onSkip() },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "Skip",
                                color = Color.White.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // CENTER: Pager Content
                OnboardingPager(
                    state = pagerState,
                    items = items,
                    isTablet = isTablet,
                    modifier = Modifier.weight(1f)
                )

                // BOTTOM AREA: Indicator & Main Action
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = contentPadding)
                        .padding(bottom = if (isLandscape && !isTablet) 16.dp else 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Page Indicator
                    AnimatedPageIndicator(
                        pageSize = items.size,
                        currentPage = currentPage
                    )

                    // Navigation Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (currentPage < items.size - 1) {
                            PremiumGradientButton(
                                text = "Next",
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(currentPage + 1)
                                    }
                                }
                            )
                        } else {
                            PremiumGradientButton(
                                text = "Start Testing",
                                onClick = { viewModel.onNext(items.size) },
                                colors = listOf(Color(0xFF00D4FF), Color(0xFF3B8BFF))
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getOnboardingItems(): List<OnboardingItem> = listOf(
    OnboardingItem(
        title = "NetPulse",
        subtitle = "Measure your internet speed with precision and monitor your network performance in real time.",
        features = listOf("Download Speed", "Upload Speed", "Ping", "Jitter"),
        icon = Icons.Outlined.Speed,
        highlightColor = Color(0xFF3B8BFF)
    ),
    OnboardingItem(
        title = "Understand Your Network",
        description = "• Live network quality\n• Wi-Fi information\n• ISP details\n• Public IP\n• DNS\n• Signal Strength\n• Device Information",
        icon = Icons.Outlined.Analytics,
        highlightColor = Color(0xFF00D4FF)
    ),
    OnboardingItem(
        title = "Track Performance Over Time",
        description = "• Speed history\n• Performance charts\n• Network trends\n• Compare previous tests\n• Export reports",
        icon = Icons.Outlined.History,
        highlightColor = Color(0xFF00E676)
    ),
    OnboardingItem(
        title = "Privacy First",
        subtitle = "We never sell your personal data. Only the information required to perform network tests is collected.",
        trustBadges = listOf(
            TrustBadge("Secure", Icons.Outlined.Security),
            TrustBadge("No Tracking", Icons.Outlined.VisibilityOff),
            TrustBadge("Ad-Free", Icons.Outlined.AdsClick)
        ),
        icon = Icons.Outlined.Shield,
        highlightColor = Color(0xFFFFB300)
    ),
    OnboardingItem(
        title = "You're Ready!",
        subtitle = "Let's test your connection and discover your internet performance.",
        icon = Icons.Outlined.RocketLaunch,
        highlightColor = Color(0xFF3B8BFF)
    )
)

@Preview(showBackground = true)
@Composable
fun OnboardingPreview() {
    NetPulseTheme {
        OnboardingScreen(onFinished = {})
    }
}

package com.example.netpulse.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.netpulse.ui.screen.onboarding.*
import com.example.netpulse.ui.theme.DarkGradient
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

    // Sync ViewModel state with PagerState
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onPageChanged(pagerState.currentPage)
    }

    // Handle navigation effects
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is OnboardingUiEffect.NavigateToHome -> onFinished()
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGradient)
    ) {
        val isTablet = maxWidth > 600.dp
        val horizontalPadding = if (isTablet) 80.dp else 24.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // TOP AREA: Skip Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (currentPage < items.size - 1) {
                    TextButton(onClick = { viewModel.onSkip() }) {
                        Text(
                            "Skip",
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // CENTER AREA: Horizontal Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) { pageIndex ->
                OnboardingPageContent(
                    item = items[pageIndex],
                    isTablet = isTablet,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }

            // BOTTOM AREA: Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Page Indicator
                AnimatedPageIndicator(
                    pageSize = items.size,
                    currentPage = currentPage
                )

                // Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (currentPage < items.size - 1) {
                        PremiumGradientButton(
                            text = "Next",
                            onClick = { 
                                viewModel.onNext(items.size)
                                // We don't animate pager here manually because LaunchedEffect syncs it if we change VM state,
                                // but for immediate feedback:
                                scope.launch { pagerState.animateScrollToPage(currentPage + 1) }
                            }
                        )
                    } else {
                        PremiumGradientButton(
                            text = "Get Started",
                            onClick = { viewModel.onNext(items.size) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(
    item: OnboardingItem,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // GLASSMORPHISM ILLUSTRATION BOX
        Box(
            modifier = Modifier
                .size(if (isTablet) 320.dp else 240.dp)
                .clip(CircleShape)
                .background(item.highlightColor.copy(alpha = 0.1f))
                .border(1.dp, item.highlightColor.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Subtle glow
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .blur(60.dp)
                    .background(item.highlightColor.copy(alpha = 0.3f))
            )

            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(if (isTablet) 120.dp else 80.dp),
                tint = item.highlightColor
            )
        }

        Spacer(modifier = Modifier.height(if (isTablet) 60.dp else 40.dp))

        // TEXT CONTENT
        Text(
            text = item.title,
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = item.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (item.features.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                item.features.forEach { feature ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = item.highlightColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = feature,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 15.sp
                        )
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
        subtitle = "Get deep insights into your network quality, Wi-Fi info, ISP details, and signal strength.",
        features = listOf("Live Network Quality", "Wi-Fi & ISP Details", "Signal Strength"),
        icon = Icons.Outlined.Analytics,
        highlightColor = Color(0xFF00D4FF)
    ),
    OnboardingItem(
        title = "Track Performance",
        subtitle = "Monitor performance over time with charts, trends, and previous test comparisons.",
        features = listOf("Speed History", "Network Trends", "Export Reports"),
        icon = Icons.Outlined.History,
        highlightColor = Color(0xFF00E676)
    ),
    OnboardingItem(
        title = "Privacy First",
        subtitle = "We never sell your personal data. Only information required for network tests is collected.",
        features = listOf("Secure & Encrypted", "No Tracking", "Ad-Free Premium"),
        icon = Icons.Outlined.Shield,
        highlightColor = Color(0xFFFFB300)
    ),
    OnboardingItem(
        title = "You're Ready!",
        subtitle = "Let's test your connection and discover your true internet performance.",
        icon = Icons.Outlined.RocketLaunch,
        highlightColor = Color(0xFF3B8BFF)
    )
)

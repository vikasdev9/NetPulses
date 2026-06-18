package com.example.netpulse.ui.screen.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingPager(
    state: PagerState,
    items: List<OnboardingItem>,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        state = state,
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        userScrollEnabled = true
    ) { pageIndex ->
        val pageOffset = (
                (state.currentPage - pageIndex) + state.currentPageOffsetFraction
                ).absoluteValue

        Box(
            modifier = Modifier.graphicsLayer {
                // Fade out when scrolling
                alpha = lerp(
                    start = 1f,
                    stop = 0f,
                    fraction = pageOffset.coerceIn(0f, 1f)
                )
                // Slight scale effect
                val scale = lerp(
                    start = 1f,
                    stop = 0.9f,
                    fraction = pageOffset.coerceIn(0f, 1f)
                )
                scaleX = scale
                scaleY = scale
                
                // Slight vertical parallax
                translationY = pageOffset * 60f
            }
        ) {
            OnboardingPageContent(
                item = items[pageIndex],
                isTablet = isTablet
            )
        }
    }
}

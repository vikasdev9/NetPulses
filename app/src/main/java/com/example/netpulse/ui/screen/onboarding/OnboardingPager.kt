package com.example.netpulse.ui.screen.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

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
        OnboardingPage(
            item = items[pageIndex],
            isTablet = isTablet
        )
    }
}

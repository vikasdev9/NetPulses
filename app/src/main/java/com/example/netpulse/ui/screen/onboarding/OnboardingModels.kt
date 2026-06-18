package com.example.netpulse.ui.screen.onboarding

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class OnboardingItem(
    val title: String,
    val subtitle: String = "",
    val description: String = "",
    val features: List<String> = emptyList(),
    val trustBadges: List<TrustBadge> = emptyList(),
    val icon: ImageVector,
    val highlightColor: Color
)

data class TrustBadge(
    val label: String,
    val icon: ImageVector
)

sealed class OnboardingUiEffect {
    object NavigateToHome : OnboardingUiEffect()
}

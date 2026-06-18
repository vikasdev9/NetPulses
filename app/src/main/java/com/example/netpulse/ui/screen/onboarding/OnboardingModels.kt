package com.example.netpulse.ui.screen.onboarding

import androidx.compose.ui.graphics.vector.ImageVector

data class OnboardingItem(
    val title: String,
    val subtitle: String,
    val features: List<String> = emptyList(),
    val icon: ImageVector,
    val highlightColor: androidx.compose.ui.graphics.Color
)

sealed class OnboardingUiEffect {
    object NavigateToHome : OnboardingUiEffect()
}

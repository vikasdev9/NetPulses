package com.example.netpulse.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel : ViewModel() {

    private val _currentPage = MutableStateFlow(0)
    val currentPage = _currentPage.asStateFlow()

    private val _uiEffect = MutableSharedFlow<OnboardingUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    fun onPageChanged(page: Int) {
        _currentPage.value = page
    }

    fun onNext(totalPages: Int) {
        if (_currentPage.value < totalPages - 1) {
            _currentPage.value += 1
        } else {
            completeOnboarding()
        }
    }

    fun onSkip() {
        completeOnboarding()
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            _uiEffect.emit(OnboardingUiEffect.NavigateToHome)
        }
    }
}

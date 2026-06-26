package com.chalkak.recap.feature.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OnboardingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.Back -> moveBack()
            OnboardingAction.Start -> moveTo(OnboardingStep.Auth)
            OnboardingAction.LoginWithKakao,
            OnboardingAction.LoginWithApple,
            OnboardingAction.LoginWithEmail,
                -> moveTo(OnboardingStep.PermissionGuide)

            OnboardingAction.GrantPermission,
            OnboardingAction.SkipPermission,
                -> moveTo(OnboardingStep.CleanupRange)

            is OnboardingAction.SelectRange -> {
                _uiState.update { current ->
                    current.copy(selectedRange = action.range)
                }
            }

            OnboardingAction.ConfirmRange -> moveTo(OnboardingStep.CleanupStart)
            OnboardingAction.StartCleanup -> Unit
        }
    }

    private fun moveTo(step: OnboardingStep) {
        _uiState.update { current ->
            current.copy(step = step, errorMessage = null)
        }
    }

    private fun moveBack() {
        _uiState.update { current ->
            current.copy(step = current.step.previousStep(), errorMessage = null)
        }
    }
}

private fun OnboardingStep.previousStep(): OnboardingStep =
    when (this) {
        OnboardingStep.Start -> OnboardingStep.Start
        OnboardingStep.Auth -> OnboardingStep.Start
        OnboardingStep.PermissionGuide -> OnboardingStep.Auth
        OnboardingStep.CleanupRange -> OnboardingStep.PermissionGuide
        OnboardingStep.CleanupStart -> OnboardingStep.CleanupRange
    }

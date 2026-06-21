package com.chalkak.recap.feature.onboarding

data class OnboardingUiState(
    val title: String = "Onboarding",
    val description: String = "Photo permission and initial screenshot range setup will start here.",
)

sealed interface OnboardingAction {
    data object Continue : OnboardingAction
}

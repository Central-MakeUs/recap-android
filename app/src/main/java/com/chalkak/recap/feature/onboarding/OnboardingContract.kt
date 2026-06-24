package com.chalkak.recap.feature.onboarding

data class OnboardingUiState(
    val title: String = "RECAP",
    val description: String = "Screenshots cleanup onboarding starts here.",
    val step: OnboardingStep = OnboardingStep.Start,
    val selectedRange: CleanupRange = CleanupRange.Last30Days,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface OnboardingAction {
    data object Back : OnboardingAction
    data object Start : OnboardingAction
    data object LoginWithKakao : OnboardingAction
    data object LoginWithApple : OnboardingAction
    data object LoginWithEmail : OnboardingAction
    data object GrantPermission : OnboardingAction
    data object SkipPermission : OnboardingAction
    data class SelectRange(val range: CleanupRange) : OnboardingAction
    data object ConfirmRange : OnboardingAction
    data object StartCleanup : OnboardingAction
}

enum class OnboardingStep {
    Start,
    Auth,
    PermissionGuide,
    CleanupRange,
    CleanupStart,
}

enum class CleanupRange(
    val title: String,
    val countLabel: String,
    val badge: String? = null,
) {
    Last7Days(title = "최근 7일", countLabel = "26개"),
    Last30Days(title = "최근 30일", countLabel = "124개", badge = "추천"),
    Last3Months(title = "최근 3개월", countLabel = "386개"),
}

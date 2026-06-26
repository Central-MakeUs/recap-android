package com.chalkak.recap.feature.onboarding

import androidx.annotation.StringRes
import com.chalkak.recap.R

data class OnboardingUiState(
    @get:StringRes val titleResId: Int = R.string.onboarding_title,
    @get:StringRes val descriptionResId: Int = R.string.onboarding_description,
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
    @get:StringRes val titleResId: Int,
    @get:StringRes val countLabelResId: Int,
    @get:StringRes val badgeResId: Int? = null,
) {
    Last7Days(
        titleResId = R.string.onboarding_cleanup_range_last_7_days,
        countLabelResId = R.string.onboarding_cleanup_range_last_7_days_count,
    ),
    Last30Days(
        titleResId = R.string.onboarding_cleanup_range_last_30_days,
        countLabelResId = R.string.onboarding_cleanup_range_last_30_days_count,
        badgeResId = R.string.onboarding_cleanup_range_recommended_badge,
    ),
    Last3Months(
        titleResId = R.string.onboarding_cleanup_range_last_3_months,
        countLabelResId = R.string.onboarding_cleanup_range_last_3_months_count,
    ),
}

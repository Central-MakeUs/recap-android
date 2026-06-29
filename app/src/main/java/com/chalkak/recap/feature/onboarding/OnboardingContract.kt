package com.chalkak.recap.feature.onboarding

import androidx.annotation.StringRes
import com.chalkak.recap.R
import com.chalkak.recap.core.model.ImageAccessLevel
import com.chalkak.recap.core.model.OcrCleanupRange
import com.chalkak.recap.core.model.OcrJob

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.Auth,
    val selectedRange: CleanupRange = CleanupRange.Last30Days,
    val imageAccessLevel: ImageAccessLevel = ImageAccessLevel.Denied,
    val rangeCounts: Map<CleanupRange, Int> = CleanupRange.entries.associateWith { 0 },
    val activeOcrJob: OcrJob? = null,
    val isRangeCountLoading: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val selectedRangeCount: Int
        get() = rangeCounts[selectedRange] ?: 0

    val canConfirmRange: Boolean
        get() = imageAccessLevel != ImageAccessLevel.Denied &&
                selectedRangeCount > 0 &&
                !isRangeCountLoading &&
                !isLoading
}

sealed interface OnboardingAction {
    data object Back : OnboardingAction
    data object LoginWithKakao : OnboardingAction
    data object LoginWithApple : OnboardingAction
    data object LoginWithEmail : OnboardingAction
    data object GrantPermission : OnboardingAction
    data object RefreshImagePermission : OnboardingAction
    data object SkipPermission : OnboardingAction
    data class SelectRange(val range: CleanupRange) : OnboardingAction
    data object ConfirmRange : OnboardingAction
    data object StartCleanup : OnboardingAction
}

enum class OnboardingStep {
    Auth,
    PermissionGuide,
    CleanupRange,
    CleanupStart,
}

enum class CleanupRange(
    @get:StringRes val titleResId: Int,
    @get:StringRes val countLabelResId: Int,
    @get:StringRes val badgeResId: Int? = null,
    val ocrRange: OcrCleanupRange,
) {
    Last7Days(
        titleResId = R.string.onboarding_cleanup_range_last_7_days,
        countLabelResId = R.string.onboarding_cleanup_range_last_7_days_count,
        ocrRange = OcrCleanupRange.Last7Days,
    ),
    Last30Days(
        titleResId = R.string.onboarding_cleanup_range_last_30_days,
        countLabelResId = R.string.onboarding_cleanup_range_last_30_days_count,
        badgeResId = R.string.onboarding_cleanup_range_recommended_badge,
        ocrRange = OcrCleanupRange.Last30Days,
    ),
    Last3Months(
        titleResId = R.string.onboarding_cleanup_range_last_3_months,
        countLabelResId = R.string.onboarding_cleanup_range_last_3_months_count,
        ocrRange = OcrCleanupRange.Last90Days,
    ),
}

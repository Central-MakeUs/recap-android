package com.chalkak.recap.feature.onboarding

import com.chalkak.recap.core.model.ImageAccessLevel

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.Landing,
    val imageAccessLevel: ImageAccessLevel = ImageAccessLevel.Denied,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface OnboardingAction {
    data object Back : OnboardingAction
    data object LoginWithKakao : OnboardingAction
    data object SelectFirstScreenshots : OnboardingAction
    data object OpenAddToFavoriteGuide : OnboardingAction
    data object SkipFirstOrganize : OnboardingAction
    data object GrantPermission : OnboardingAction
    data object OpenPhotoPermissionSettings : OnboardingAction
    data object RefreshImagePermission : OnboardingAction
    data object SkipPermission : OnboardingAction
    data object OpenScreenshotPicker : OnboardingAction
    data object SkipStartFirstAnalyze : OnboardingAction
    data object ConfirmUploadMethodGuide : OnboardingAction
}

sealed interface OnboardingEvent {
    data class ShowLoginError(
        val isCancelled: Boolean,
    ) : OnboardingEvent
}

enum class OnboardingStep {
    Landing,
    PermissionGuide,
    UploadMethodGuide,
    AddToFavorite,
    StartFirstAnalyze,
}

sealed interface OnboardingIllustrationSignal {
    data object Blink : OnboardingIllustrationSignal
}

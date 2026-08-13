package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingUiState

@PreviewTest
@QaPhoneMatrix
@Composable
fun OnboardingAddToFavoriteScreenScreenshot() {
    OnboardingPreviewContainer {
        OnboardingAddToFavoriteScreen(
            uiState = OnboardingUiState(),
            onAction = {},
        )
    }
}

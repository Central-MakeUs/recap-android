package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.component.OnboardingLayoutDefaults

@PreviewTest
@QaPhoneMatrix
@Composable
fun OnboardingLandingScreenScreenshot() {
    OnboardingPreviewContainer(
        contentPadding = OnboardingLayoutDefaults.LandingScreenPadding,
    ) {
        OnboardingLandingScreen(onAction = {})
    }
}

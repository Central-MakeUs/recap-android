package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer

@PreviewTest
@QaPhoneMatrix
@Composable
fun OnboardingPermissionGuideScreenScreenshot() {
    OnboardingPreviewContainer {
        OnboardingPermissionGuideScreen(
            hasResolvedPermissionStep = false,
            onAction = {},
        )
    }
}

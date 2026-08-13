package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.core.design.theme.RECAPTheme

@PreviewTest
@QaPhoneMatrix
@Composable
fun OnboardingAddToFavoriteGuideScreenStep1Screenshot() {
    AddToFavoriteGuideScreenshot(initialPage = 0)
}

@PreviewTest
@QaPhoneMatrix
@Composable
fun OnboardingAddToFavoriteGuideScreenStep2Screenshot() {
    AddToFavoriteGuideScreenshot(initialPage = 1)
}

@PreviewTest
@QaPhoneMatrix
@Composable
fun OnboardingAddToFavoriteGuideScreenStep3Screenshot() {
    AddToFavoriteGuideScreenshot(initialPage = 2)
}

@PreviewTest
@QaPhoneMatrix
@Composable
fun OnboardingAddToFavoriteGuideScreenStep4Screenshot() {
    AddToFavoriteGuideScreenshot(initialPage = 3)
}

@Composable
private fun AddToFavoriteGuideScreenshot(initialPage: Int) {
    RECAPTheme(dynamicColor = false) {
        OnboardingAddToFavoriteGuideScreen(
            onBackClick = {},
            initialPage = initialPage,
        )
    }
}

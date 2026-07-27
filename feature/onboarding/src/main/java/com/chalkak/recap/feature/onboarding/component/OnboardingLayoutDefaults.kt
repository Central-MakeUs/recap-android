package com.chalkak.recap.feature.onboarding.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

@Immutable
internal object OnboardingLayoutDefaults {
    val ScreenHorizontalPadding = 24.dp
    val ScreenVerticalPadding = 24.dp

    val ScreenPadding = PaddingValues(
        horizontal = ScreenHorizontalPadding,
        vertical = ScreenVerticalPadding,
    )

    /** 랜딩은 TopBar가 없어 상·하단 ScreenPadding을 두지 않는다. */
    val LandingScreenPadding = PaddingValues(
        horizontal = ScreenHorizontalPadding,
    )
}

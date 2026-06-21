package com.chalkak.recap.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chalkak.recap.core.design.component.RecapPlaceholderScreen

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState = OnboardingUiState(),
    modifier: Modifier = Modifier,
    onAction: (OnboardingAction) -> Unit = {},
) {
    RecapPlaceholderScreen(
        title = uiState.title,
        description = uiState.description,
        modifier = modifier,
    )
}

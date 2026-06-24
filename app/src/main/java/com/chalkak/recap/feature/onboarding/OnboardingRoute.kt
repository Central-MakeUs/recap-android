package com.chalkak.recap.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun OnboardingRoute(
    onOnboardingComplete: () -> Unit,
    viewModelKey: String? = null,
    viewModel: OnboardingViewModel = viewModel(key = viewModelKey),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingScreen(
        uiState = uiState,
        onAction = { action ->
            viewModel.onAction(action)
            if (action == OnboardingAction.StartCleanup) {
                onOnboardingComplete()
            }
        },
    )
}

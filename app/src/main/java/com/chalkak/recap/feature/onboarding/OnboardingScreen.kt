package com.chalkak.recap.feature.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.feature.onboarding.screen.OnboardingImagePolicyScreen
import com.chalkak.recap.feature.onboarding.screen.OnboardingLandingScreen
import com.chalkak.recap.feature.onboarding.screen.OnboardingLoginScreen
import com.chalkak.recap.feature.onboarding.screen.OnboardingFirstCleanupScreen
import com.chalkak.recap.feature.onboarding.screen.OnboardingCleanupRangeScreen
import com.chalkak.recap.feature.onboarding.screen.OnboardingCleanupStartScreen

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            when (uiState.step) {
                OnboardingStep.Landing -> OnboardingLandingScreen(
                    onAction = onAction,
                )

                OnboardingStep.ImagePolicy -> OnboardingImagePolicyScreen(
                    onAction = onAction,
                )

                OnboardingStep.Login -> OnboardingLoginScreen(
                    onAction = onAction,
                )

                OnboardingStep.FirstCleanup -> OnboardingFirstCleanupScreen(
                    onAction = onAction,
                )

                OnboardingStep.CleanupRange -> OnboardingCleanupRangeScreen(
                    uiState = uiState,
                    onAction = onAction,
                )

                OnboardingStep.CleanupStart -> OnboardingCleanupStartScreen(
                    uiState = uiState,
                    onAction = onAction,
                )
            }
        }
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingScreenAuthPreview() {
    RECAPTheme(dynamicColor = false) {
        OnboardingScreen(
            uiState = OnboardingUiState(step = OnboardingStep.Landing),
            onAction = {},
        )
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingScreenPermissionGuidePreview() {
    RECAPTheme(dynamicColor = false) {
        OnboardingScreen(
            uiState = OnboardingUiState(step = OnboardingStep.ImagePolicy),
            onAction = {},
        )
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingScreenLoginPreview() {
    RECAPTheme(dynamicColor = false) {
        OnboardingScreen(
            uiState = OnboardingUiState(step = OnboardingStep.Login),
            onAction = {},
        )
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingScreenFirstCleanupPreview() {
    RECAPTheme(dynamicColor = false) {
        OnboardingScreen(
            uiState = OnboardingUiState(step = OnboardingStep.FirstCleanup),
            onAction = {},
        )
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingScreenCleanupRangePreview() {
    RECAPTheme(dynamicColor = false) {
        OnboardingScreen(
            uiState = OnboardingUiState(
                step = OnboardingStep.CleanupRange,
                selectedRange = CleanupRange.Last30Days,
            ),
            onAction = {},
        )
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingScreenCleanupStartPreview() {
    RECAPTheme(dynamicColor = false) {
        OnboardingScreen(
            uiState = OnboardingUiState(
                step = OnboardingStep.CleanupStart,
                selectedRange = CleanupRange.Last30Days,
            ),
            onAction = {},
        )
    }
}

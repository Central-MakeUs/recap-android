package com.chalkak.recap.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.feature.onboarding.component.OnboardingLayoutDefaults
import com.chalkak.recap.feature.onboarding.screen.OnboardingCleanupRangeScreen
import com.chalkak.recap.feature.onboarding.screen.OnboardingCleanupStartScreen
import com.chalkak.recap.feature.onboarding.screen.OnboardingFirstCleanupScreen
import com.chalkak.recap.feature.onboarding.screen.OnboardingImagePolicyScreen
import com.chalkak.recap.feature.onboarding.screen.OnboardingLandingScreen
import com.chalkak.recap.feature.onboarding.screen.OnboardingPermissionGuideScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    showLandingLoginImmediately: Boolean = false,
    illustrationSignalFlow: Flow<OnboardingIllustrationSignal> = emptyFlow(),
) {
    val resolvedSnackbarHostState = snackbarHostState ?: remember { SnackbarHostState() }

    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding(),
        ) {
            val screenModifier = Modifier
                .fillMaxSize()
                .padding(OnboardingLayoutDefaults.ScreenPadding)

            when (uiState.step) {
                OnboardingStep.Landing -> OnboardingLandingScreen(
                    onAction = onAction,
                    modifier = screenModifier,
                    showLoginImmediately = showLandingLoginImmediately,
                    illustrationSignalFlow = illustrationSignalFlow,
                )

                OnboardingStep.ImagePolicy -> OnboardingImagePolicyScreen(
                    onAction = onAction,
                    modifier = screenModifier,
                )

                OnboardingStep.PermissionGuide -> OnboardingPermissionGuideScreen(
                    onAction = onAction,
                    modifier = screenModifier,
                )

                OnboardingStep.FirstCleanup -> OnboardingFirstCleanupScreen(
                    uiState = uiState,
                    onAction = onAction,
                    modifier = screenModifier,
                )

                OnboardingStep.CleanupRange -> OnboardingCleanupRangeScreen(
                    uiState = uiState,
                    onAction = onAction,
                    modifier = screenModifier,
                )

                OnboardingStep.CleanupStart -> OnboardingCleanupStartScreen(
                    uiState = uiState,
                    onAction = onAction,
                    modifier = screenModifier,
                )
            }
            SnackbarHost(
                hostState = resolvedSnackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
            )
        }
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingScreenLandingPreview() {
    RECAPTheme(dynamicColor = false) {
        OnboardingScreen(
            uiState = OnboardingUiState(step = OnboardingStep.Landing),
            onAction = {},
            showLandingLoginImmediately = true,
        )
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingScreenPermissionGuidePreview() {
    RECAPTheme(dynamicColor = false) {
        OnboardingScreen(
            uiState = OnboardingUiState(step = OnboardingStep.PermissionGuide),
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

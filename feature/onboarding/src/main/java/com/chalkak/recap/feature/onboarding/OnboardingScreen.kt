package com.chalkak.recap.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.feature.onboarding.component.OnboardingLayoutDefaults
import com.chalkak.recap.feature.onboarding.component.OnboardingTopBar
import com.chalkak.recap.feature.onboarding.screen.OnboardingAddToFavoriteScreen
import com.chalkak.recap.feature.onboarding.screen.OnboardingLandingScreen
import com.chalkak.recap.feature.onboarding.screen.OnboardingPermissionGuideScreen
import com.chalkak.recap.feature.onboarding.screen.OnboardingStartFirstAnalyzeScreen
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

                OnboardingStep.PermissionGuide,
                OnboardingStep.AddToFavorite,
                OnboardingStep.StartFirstAnalyze -> OnboardingStepTransition(
                    uiState = uiState,
                    onAction = onAction,
                    modifier = Modifier.fillMaxSize(),
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

@Composable
private fun OnboardingStepTransition(
    uiState: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val topBarProgress by animateFloatAsState(
        targetValue = uiState.step.toOnboardingProgressIndex()?.toFloat() ?: 0f,
        animationSpec = tween(durationMillis = OnboardingStepTransitionDurationMillis),
        label = "onboarding_top_bar_progress",
    )

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        OnboardingTopBar(
            progress = topBarProgress,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        )
        AnimatedContent(
            targetState = uiState.step,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            transitionSpec = {
                val initialIndex = initialState.toOnboardingProgressIndex() ?: 0
                val targetIndex = targetState.toOnboardingProgressIndex() ?: initialIndex
                val direction = if (targetIndex >= initialIndex) 1 else -1

                slideInHorizontally(
                    animationSpec = tween(OnboardingStepTransitionDurationMillis),
                    initialOffsetX = { fullWidth -> direction * fullWidth },
                ) + fadeIn(
                    animationSpec = tween(OnboardingStepTransitionDurationMillis),
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(OnboardingStepTransitionDurationMillis),
                    targetOffsetX = { fullWidth -> -direction * fullWidth },
                ) + fadeOut(
                    animationSpec = tween(OnboardingStepTransitionDurationMillis),
                ) using SizeTransform(clip = false)
            },
            label = "onboarding_step_content",
        ) { step ->
            val pageModifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)

            when (step) {
                OnboardingStep.PermissionGuide -> OnboardingPermissionGuideScreen(
                    onAction = onAction,
                    modifier = pageModifier,
                )

                OnboardingStep.AddToFavorite -> OnboardingAddToFavoriteScreen(
                    uiState = uiState,
                    onAction = onAction,
                    modifier = pageModifier,
                )

                OnboardingStep.StartFirstAnalyze -> OnboardingStartFirstAnalyzeScreen(
                    uiState = uiState,
                    onAction = onAction,
                    modifier = pageModifier,
                )

                OnboardingStep.Landing -> Unit
            }
        }
    }
}

private const val OnboardingStepTransitionDurationMillis = 300

private val OnboardingProgressSteps = listOf(
    OnboardingStep.PermissionGuide,
    OnboardingStep.AddToFavorite,
    OnboardingStep.StartFirstAnalyze,
)

private fun OnboardingStep.toOnboardingProgressIndex(): Int? =
    OnboardingProgressSteps.indexOf(this).takeIf { it >= 0 }

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
private fun OnboardingScreenAddToFavoritePreview() {
    RECAPTheme(dynamicColor = false) {
        OnboardingScreen(
            uiState = OnboardingUiState(step = OnboardingStep.AddToFavorite),
            onAction = {},
        )
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingScreenStartFirstAnalyzePreview() {
    RECAPTheme(dynamicColor = false) {
        OnboardingScreen(
            uiState = OnboardingUiState(step = OnboardingStep.StartFirstAnalyze),
            onAction = {},
        )
    }
}

package com.chalkak.recap.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.feature.onboarding.component.OnboardingLayoutDefaults
import com.chalkak.recap.core.design.component.progress.RecapStepProgressIndicator
import com.chalkak.recap.feature.onboarding.screen.OnboardingAddToFavoriteScreen
import com.chalkak.recap.feature.onboarding.screen.OnboardingLandingScreen
import com.chalkak.recap.feature.onboarding.screen.OnboardingPermissionGuideScreen
import com.chalkak.recap.feature.onboarding.screen.OnboardingStartFirstAnalyzeScreen
import com.chalkak.recap.feature.onboarding.screen.OnboardingUploadMethodGuideScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    illustrationSignalFlow: Flow<OnboardingIllustrationSignal> = emptyFlow(),
) {
    val resolvedSnackbarHostState = snackbarHostState ?: remember { SnackbarHostState() }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding(),
        ) {
            when (uiState.step) {
                OnboardingStep.Landing -> OnboardingLandingScreen(
                    onAction = onAction,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(OnboardingLayoutDefaults.LandingScreenPadding),
                    isLoading = uiState.isLoading,
                    illustrationSignalFlow = illustrationSignalFlow,
                )

                OnboardingStep.PermissionGuide,
                OnboardingStep.UploadMethodGuide,
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
    val initialPage = uiState.step.toOnboardingProgressIndex() ?: 0
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { OnboardingProgressSteps.size },
    )

    LaunchedEffect(uiState.step) {
        val targetPage = uiState.step.toOnboardingProgressIndex() ?: return@LaunchedEffect
        if (pagerState.currentPage != targetPage || pagerState.targetPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .map { page -> OnboardingProgressSteps.getOrNull(page) }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { step ->
                onAction(OnboardingAction.SelectStep(step))
            }
    }

    val topBarProgress = pagerState.currentPage + pagerState.currentPageOffsetFraction

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        RecapStepProgressIndicator(
            progress = topBarProgress,
            stepCount = OnboardingProgressSteps.size,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { page ->
            val pageModifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)

            when (OnboardingProgressSteps[page]) {
                OnboardingStep.PermissionGuide -> OnboardingPermissionGuideScreen(
                    hasResolvedPermissionStep = uiState.hasResolvedPermissionStep,
                    onAction = onAction,
                    modifier = pageModifier,
                )

                OnboardingStep.UploadMethodGuide -> OnboardingUploadMethodGuideScreen(
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

private val OnboardingProgressSteps = listOf(
    OnboardingStep.PermissionGuide,
    OnboardingStep.UploadMethodGuide,
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
private fun OnboardingScreenUploadMethodGuidePreview() {
    RECAPTheme(dynamicColor = false) {
        OnboardingScreen(
            uiState = OnboardingUiState(step = OnboardingStep.UploadMethodGuide),
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

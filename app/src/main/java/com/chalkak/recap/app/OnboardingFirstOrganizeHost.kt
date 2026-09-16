package com.chalkak.recap.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chalkak.recap.core.model.observability.OrganizeTraceEntry
import com.chalkak.recap.feature.onboarding.OnboardingRoute
import com.chalkak.recap.feature.organize.OrganizeAnalysisStatusRoute
import com.chalkak.recap.feature.organize.OrganizeAnalysisStatusUiState
import com.chalkak.recap.feature.organize.OrganizeRoute
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

@Composable
fun OnboardingFirstOrganizeHost(
    analysisProgressViewModel: ScreenshotAnalysisProgressViewModel,
    onCompleteOnboarding: () -> Unit,
    onboardingSessionKey: Int,
    pendingSampleShareAdvanceRequestIds: StateFlow<Int?>,
    onSampleShareAdvanceComplete: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var phase by rememberSaveable {
        mutableStateOf(OnboardingFirstOrganizePhase.Idle)
    }
    val analysisStatusFlow = remember(analysisProgressViewModel) {
        analysisProgressViewModel.uiState.map { state ->
            state.toOrganizeAnalysisStatusUiState()
        }
    }
    val initialAnalysisStatus = remember(analysisProgressViewModel) {
        analysisProgressViewModel.uiState.value.toOrganizeAnalysisStatusUiState()
    }
    val analysisStatus by analysisStatusFlow.collectAsStateWithLifecycle(
        initialValue = initialAnalysisStatus,
    )
    var renderedAnalysisStatus by remember {
        mutableStateOf<OrganizeAnalysisStatusUiState?>(null)
    }
    val organizeCompleteNotificationEnabled by
        analysisProgressViewModel.organizeCompleteNotificationEnabled
            .collectAsStateWithLifecycle()

    LaunchedEffect(analysisStatus, phase) {
        if (phase == OnboardingFirstOrganizePhase.AnalysisSession) {
            if (analysisStatus !is OrganizeAnalysisStatusUiState.Hidden) {
                renderedAnalysisStatus = analysisStatus
            }
        } else {
            renderedAnalysisStatus = null
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        OnboardingRoute(
            onOnboardingComplete = onCompleteOnboarding,
            onOpenScreenshotPicker = {
                phase = reduceOnboardingFirstOrganize(
                    phase = phase,
                    event = OnboardingFirstOrganizeEvent.OpenPicker,
                )
            },
            viewModelKey = "onboarding-$onboardingSessionKey",
            pendingSampleShareAdvanceRequestIds = pendingSampleShareAdvanceRequestIds,
            onSampleShareAdvanceComplete = onSampleShareAdvanceComplete,
        )

        if (phase == OnboardingFirstOrganizePhase.OrganizeOverlay) {
            OrganizeRoute(
                onNavigateBack = {
                    phase = reduceOnboardingFirstOrganize(
                        phase = phase,
                        event = OnboardingFirstOrganizeEvent.DismissOrganize,
                    )
                },
                onOrganizeComplete = { candidates ->
                    phase = reduceOnboardingFirstOrganize(
                        phase = phase,
                        event = OnboardingFirstOrganizeEvent.StartAnalysis,
                    )
                    renderedAnalysisStatus = null
                    analysisProgressViewModel.startAnalysis(
                        candidates = candidates,
                        entry = OrganizeTraceEntry.ONBOARDING_FIRST,
                    )
                },
            )
        }

        if (phase == OnboardingFirstOrganizePhase.AnalysisSession) {
            BackHandler {
                if (analysisProgressViewModel.uiState.value.isRunning) {
                    analysisProgressViewModel.cancelAnalysis()
                    phase = reduceOnboardingFirstOrganize(
                        phase = phase,
                        event = OnboardingFirstOrganizeEvent.CancelAnalysis,
                    )
                }
            }

            renderedAnalysisStatus?.let { status ->
                OrganizeAnalysisStatusRoute(
                    uiState = status,
                    onCancelClick = {
                        analysisProgressViewModel.cancelAnalysis()
                        phase = reduceOnboardingFirstOrganize(
                            phase = phase,
                            event = OnboardingFirstOrganizeEvent.CancelAnalysis,
                        )
                    },
                    onDismissClick = {
                        analysisProgressViewModel.dismissResult()
                        phase = reduceOnboardingFirstOrganize(
                            phase = phase,
                            event = OnboardingFirstOrganizeEvent.DismissTerminalResult,
                        )
                        onCompleteOnboarding()
                    },
                    organizeCompleteNotificationEnabled =
                        organizeCompleteNotificationEnabled,
                    onOrganizeCompleteNotificationEnabledChange =
                        analysisProgressViewModel::setOrganizeCompleteNotificationEnabled,
                    onTryMarkOrganizeNotificationPermissionPromptShown =
                        analysisProgressViewModel::tryMarkOrganizeNotificationPermissionPromptShown,
                )
            }
        }
    }
}

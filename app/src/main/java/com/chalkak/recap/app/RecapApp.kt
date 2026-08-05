package com.chalkak.recap.app

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.animation.RecapNavDisplay
import com.chalkak.recap.core.design.animation.RecapNavigationMotion
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBarDefaults
import com.chalkak.recap.core.design.component.popup.RecapPopup
import com.chalkak.recap.core.design.component.systembar.RecapNavigationBarGradientScrim
import com.chalkak.recap.core.design.component.toast.ProvideRecapToastDispatcher
import com.chalkak.recap.core.design.component.toast.RecapToastDispatcher
import com.chalkak.recap.core.design.component.toast.RecapToastDuration
import com.chalkak.recap.core.design.component.toast.RecapToastHost
import com.chalkak.recap.core.design.component.toast.RecapToastRequest
import com.chalkak.recap.core.design.component.toast.RecapToastType
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBackground
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.feature.developer.DeveloperRoute
import com.chalkak.recap.feature.onboarding.ReauthRoute
import dev.chrisbanes.haze.HazePositionStrategy
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.StateFlow

private const val RecapSplashToAppFadeMillis = 300

@Composable
fun RecapApp(
    startupViewModel: RecapStartupViewModel,
    toastViewModel: RecapToastViewModel,
    analysisProgressViewModel: ScreenshotAnalysisProgressViewModel,
    pendingHomeNavigationRequestId: Int?,
    onRequestNavigateHome: () -> Unit,
    onHomeNavigationComplete: (Int) -> Unit,
    pendingOnboardingSampleShareAdvanceRequestIds: StateFlow<Int?>,
    onOnboardingSampleShareAdvanceComplete: (Int) -> Unit,
) {
    RECAPTheme {
        val uiState by startupViewModel.uiState.collectAsStateWithLifecycle()
        var lottieSplashComplete by rememberSaveable { mutableStateOf(false) }
        var showSplashOverlay by rememberSaveable { mutableStateOf(true) }
        var onboardingSessionKey by rememberSaveable { mutableIntStateOf(0) }
        val canEnterApp = canEnterRecapApp(lottieSplashComplete, uiState)
        val showStartupReadError =
            lottieSplashComplete && uiState is RecapStartupUiState.ReadError

        LaunchedEffect(canEnterApp) {
            if (canEnterApp) {
                showSplashOverlay = false
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            val readyUiState = uiState as? RecapStartupUiState.Ready
            AnimatedVisibility(
                visible = canEnterApp && readyUiState != null,
                enter = fadeIn(animationSpec = tween(RecapSplashToAppFadeMillis)),
                modifier = Modifier.fillMaxSize(),
            ) {
                if (readyUiState != null) {
                    RecapAppReadyContent(
                        readyState = readyUiState,
                        startupViewModel = startupViewModel,
                        toastViewModel = toastViewModel,
                        analysisProgressViewModel = analysisProgressViewModel,
                        pendingHomeNavigationRequestId = pendingHomeNavigationRequestId,
                        onRequestNavigateHome = onRequestNavigateHome,
                        onHomeNavigationComplete = onHomeNavigationComplete,
                        pendingOnboardingSampleShareAdvanceRequestIds =
                            pendingOnboardingSampleShareAdvanceRequestIds,
                        onOnboardingSampleShareAdvanceComplete =
                            onOnboardingSampleShareAdvanceComplete,
                        onboardingSessionKey = onboardingSessionKey,
                        onOnboardingSessionKeyChange = { onboardingSessionKey = it },
                    )
                }
            }

            AnimatedVisibility(
                visible = (showSplashOverlay || !canEnterApp) && !showStartupReadError,
                exit = fadeOut(animationSpec = tween(RecapSplashToAppFadeMillis)),
                modifier = Modifier.fillMaxSize(),
            ) {
                RecapLottieSplashScreen(
                    skipAnimation = lottieSplashComplete,
                    onSplashFinished = { lottieSplashComplete = true },
                )
            }

            if (showStartupReadError) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(RecapBackground),
                ) {
                    RecapPopup(
                        title = stringResource(R.string.startup_read_error_title),
                        description = stringResource(R.string.startup_read_error_description),
                        confirmButtonText = stringResource(R.string.startup_read_error_retry),
                        onConfirmClick = startupViewModel::retryStartup,
                        onDismissRequest = {},
                        confirmButtonColor = RecapBlue300,
                        properties = DialogProperties(
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false,
                            usePlatformDefaultWidth = false,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun RecapAppReadyContent(
    readyState: RecapStartupUiState.Ready,
    startupViewModel: RecapStartupViewModel,
    toastViewModel: RecapToastViewModel,
    analysisProgressViewModel: ScreenshotAnalysisProgressViewModel,
    pendingHomeNavigationRequestId: Int?,
    onRequestNavigateHome: () -> Unit,
    onHomeNavigationComplete: (Int) -> Unit,
    pendingOnboardingSampleShareAdvanceRequestIds: StateFlow<Int?>,
    onOnboardingSampleShareAdvanceComplete: (Int) -> Unit,
    onboardingSessionKey: Int,
    onOnboardingSessionKeyChange: (Int) -> Unit,
) {
    val rootBackStack = rememberNavBackStack(readyState.entryMode.toRootRoute())
    val activity = LocalActivity.current
    val context = LocalContext.current
    val toastDispatcher = remember(toastViewModel, context) {
        object : RecapToastDispatcher {
            override fun showToast(
                message: String,
                type: RecapToastType,
                duration: RecapToastDuration,
            ) {
                toastViewModel.enqueue(
                    RecapToastRequest(
                        message = message,
                        type = type,
                        durationMillis = resolveEffectiveToastDurationMillis(context, duration),
                    ),
                )
            }
        }
    }
    val toastHazeState = rememberHazeState(positionStrategy = HazePositionStrategy.Screen)
    val currentToast by toastViewModel.currentToast.collectAsStateWithLifecycle()
    val navigationBarBottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val imeBottomPadding = WindowInsets.ime
        .asPaddingValues()
        .calculateBottomPadding()
    val defaultToastBottomPadding = RecapBottomBarDefaults.Height +
        RecapBottomBarDefaults.BottomPadding +
        navigationBarBottomPadding +
        8.dp
    val toastBottomPadding = maxOf(defaultToastBottomPadding, imeBottomPadding + 8.dp)

    val sessionExpiredMessage = stringResource(R.string.reauth_session_expired_notice)

    LaunchedEffect(readyState.entryMode) {
        val targetRoute = readyState.entryMode.toRootRoute()
        val currentRoute = rootBackStack.lastOrNull()

        if (targetRoute == RecapRootRoute.Reauth) {
            // 세션이 폐기된 뒤에는 남은 업로드/분석을 이어갈 수 없다.
            analysisProgressViewModel.cancelAnalysis()
            toastDispatcher.showToast(
                message = sessionExpiredMessage,
                type = RecapToastType.Error,
            )
        }
        if (currentRoute == targetRoute) return@LaunchedEffect

        if (targetRoute == RecapRootRoute.Onboarding &&
            (currentRoute == RecapRootRoute.Main || currentRoute == RecapRootRoute.Reauth)
        ) {
            onOnboardingSessionKeyChange(onboardingSessionKey + 1)
        }
        rootBackStack.clear()
        rootBackStack.add(targetRoute)
    }

    LaunchedEffect(pendingHomeNavigationRequestId, readyState.entryMode) {
        if (pendingHomeNavigationRequestId != null &&
            readyState.entryMode == RecapEntryMode.Main
        ) {
            if (rootBackStack.lastOrNull() != RecapRootRoute.Main) {
                rootBackStack.clear()
                rootBackStack.add(RecapRootRoute.Main)
            }
        }
    }

    ProvideRecapToastDispatcher(dispatcher = toastDispatcher) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(RecapBackground)
                    .hazeSource(state = toastHazeState),
            ) {
                RecapNavDisplay(
                    backStack = rootBackStack,
                    onBack = { rootBackStack.removeLastOrNull() },
                    transitionSpec = { RecapNavigationMotion.forward() },
                    popTransitionSpec = { RecapNavigationMotion.pop() },
                    entryProvider = { route ->
                        when (route) {
                            RecapRootRoute.Onboarding -> NavEntry(route) {
                                key(onboardingSessionKey) {
                                    OnboardingFirstOrganizeHost(
                                        analysisProgressViewModel = analysisProgressViewModel,
                                        onCompleteOnboarding = startupViewModel::completeOnboarding,
                                        onboardingSessionKey = onboardingSessionKey,
                                        pendingSampleShareAdvanceRequestIds =
                                            pendingOnboardingSampleShareAdvanceRequestIds,
                                        onSampleShareAdvanceComplete =
                                            onOnboardingSampleShareAdvanceComplete,
                                    )
                                }
                            }

                            RecapRootRoute.Reauth -> NavEntry(route) {
                                ReauthRoute(
                                    onExitApp = { activity?.finish() },
                                )
                            }

                            RecapRootRoute.Main -> NavEntry(route) {
                                RecapNavHost(
                                    onNavigateToDeveloper = {
                                        rootBackStack.add(RecapRootRoute.Developer)
                                    },
                                    analysisProgressViewModel = analysisProgressViewModel,
                                    pendingHomeNavigationRequestId =
                                        pendingHomeNavigationRequestId.takeIf {
                                            rootBackStack.lastOrNull() == RecapRootRoute.Main
                                        },
                                    onRequestNavigateHome = onRequestNavigateHome,
                                    onHomeNavigationComplete = onHomeNavigationComplete,
                                )
                            }

                            RecapRootRoute.Developer -> NavEntry(route) {
                                DeveloperRoute(
                                    onResetOnboarding = {
                                        onOnboardingSessionKeyChange(onboardingSessionKey + 1)
                                        startupViewModel.resetOnboarding()
                                    },
                                )
                            }

                            else -> error("Unknown root route: $route")
                        }
                    },
                )
            }

            RecapNavigationBarGradientScrim(
                modifier = Modifier.align(Alignment.BottomCenter),
            )

            RecapToastHost(
                currentToast = currentToast,
                hazeState = toastHazeState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = toastBottomPadding),
            )
        }
    }
}

package com.chalkak.recap.app

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import com.chalkak.recap.core.design.animation.RecapNavDisplay
import com.chalkak.recap.core.design.animation.RecapNavigationMotion
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBarDefaults
import com.chalkak.recap.core.design.component.systembar.RecapNavigationBarGradientScrim
import com.chalkak.recap.core.design.component.toast.ProvideRecapToastDispatcher
import com.chalkak.recap.core.design.component.toast.RecapToastDispatcher
import com.chalkak.recap.core.design.component.toast.RecapToastDuration
import com.chalkak.recap.core.design.component.toast.RecapToastHost
import com.chalkak.recap.core.design.component.toast.RecapToastRequest
import com.chalkak.recap.core.design.component.toast.RecapToastType
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.feature.developer.DeveloperRoute
import com.chalkak.recap.feature.onboarding.OnboardingRoute
import dev.chrisbanes.haze.HazePositionStrategy
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun RecapApp(
    startupViewModel: RecapStartupViewModel,
    toastViewModel: RecapToastViewModel,
) {
    RECAPTheme {
        val uiState by startupViewModel.uiState.collectAsStateWithLifecycle()
        var onboardingSessionKey by rememberSaveable { mutableIntStateOf(0) }

        if (uiState is RecapStartupUiState.Loading) {
            return@RECAPTheme
        }

        val readyState = uiState as RecapStartupUiState.Ready
        val pendingOpenOrganize by startupViewModel.pendingOpenOrganize.collectAsStateWithLifecycle()
        val initialRoute = if (readyState.onboardingCompleted) {
            RecapRootRoute.Main
        } else {
            RecapRootRoute.Onboarding
        }
        val rootBackStack = rememberNavBackStack(initialRoute)
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

        LaunchedEffect(readyState.onboardingCompleted) {
            val targetRoute = if (readyState.onboardingCompleted) {
                RecapRootRoute.Main
            } else {
                RecapRootRoute.Onboarding
            }
            if (!readyState.onboardingCompleted &&
                rootBackStack.lastOrNull() == RecapRootRoute.Main
            ) {
                onboardingSessionKey += 1
            }
            if (rootBackStack.lastOrNull() != targetRoute) {
                rootBackStack.clear()
                rootBackStack.add(targetRoute)
            }
        }

        ProvideRecapToastDispatcher(dispatcher = toastDispatcher) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
                                    OnboardingRoute(
                                        onOnboardingComplete = startupViewModel::completeOnboarding,
                                        viewModelKey = "onboarding-$onboardingSessionKey",
                                    )
                                }

                                RecapRootRoute.Main -> NavEntry(route) {
                                    RecapNavHost(
                                        onNavigateToDeveloper = {
                                            rootBackStack.add(RecapRootRoute.Developer)
                                        },
                                        pendingOpenOrganize = pendingOpenOrganize,
                                        onPendingOpenOrganizeConsumed =
                                            startupViewModel::consumePendingOpenOrganize,
                                    )
                                }

                                RecapRootRoute.Developer -> NavEntry(route) {
                                    DeveloperRoute(
                                        onResetOnboarding = {
                                            onboardingSessionKey += 1
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
}

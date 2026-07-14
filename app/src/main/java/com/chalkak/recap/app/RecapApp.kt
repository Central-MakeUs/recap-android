package com.chalkak.recap.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.chalkak.recap.BuildConfig
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.feature.developer.DeveloperRoute
import com.chalkak.recap.feature.onboarding.OnboardingRoute

@Composable
fun RecapApp(
    startupViewModel: RecapStartupViewModel,
) {
    RECAPTheme {
        val uiState by startupViewModel.uiState.collectAsStateWithLifecycle()
        var onboardingSessionKey by rememberSaveable { mutableIntStateOf(0) }

        if (uiState is RecapStartupUiState.Loading) {
            return@RECAPTheme
        }

        val readyState = uiState as RecapStartupUiState.Ready
        val initialRoute = if (readyState.onboardingCompleted) {
            RecapRootRoute.Main
        } else {
            RecapRootRoute.Onboarding
        }
        val rootBackStack = rememberNavBackStack(initialRoute)

        LaunchedEffect(readyState.onboardingCompleted) {
            val targetRoute = if (readyState.onboardingCompleted) {
                RecapRootRoute.Main
            } else {
                RecapRootRoute.Onboarding
            }
            if (rootBackStack.lastOrNull() != targetRoute) {
                rootBackStack.clear()
                rootBackStack.add(targetRoute)
            }
        }

        NavDisplay(
            backStack = rootBackStack,
            onBack = { rootBackStack.removeLastOrNull() },
            entryProvider = { route ->
                when (route) {
                    RecapRootRoute.Onboarding -> NavEntry(route) {
                        OnboardingRoute(
                            onOnboardingComplete = startupViewModel::completeOnboarding,
                            isDebugBuild = BuildConfig.DEBUG,
                            viewModelKey = "onboarding-$onboardingSessionKey",
                        )
                    }

                    RecapRootRoute.Main -> NavEntry(route) {
                        RecapNavHost(
                            onNavigateToDeveloper = {
                                rootBackStack.add(RecapRootRoute.Developer)
                            },
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
}

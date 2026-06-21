package com.chalkak.recap.app

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.feature.demo.DemoScreen
import com.chalkak.recap.feature.onboarding.OnboardingScreen

@Composable
fun RecapApp() {
    RECAPTheme {
        val rootBackStack = rememberNavBackStack(RecapRootRoute.Main)

        NavDisplay(
            backStack = rootBackStack,
            onBack = { rootBackStack.removeLastOrNull() },
            entryProvider = { route ->
                when (route) {
                    RecapRootRoute.Onboarding -> NavEntry(route) {
                        OnboardingScreen()
                    }
                    RecapRootRoute.Main -> NavEntry(route) {
                        RecapMainScreen(
                            onNavigateToDemo = {
                                rootBackStack.add(RecapRootRoute.Demo)
                            },
                        )
                    }
                    RecapRootRoute.Demo -> NavEntry(route) {
                        DemoScreen()
                    }
                    else -> error("Unknown root route: $route")
                }
            },
        )
    }
}

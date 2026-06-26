package com.chalkak.recap.feature.developer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.chalkak.recap.feature.demo.DemoScreen
import kotlinx.serialization.Serializable

@Composable
fun DeveloperRoute(
    onResetOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(DeveloperDestination.Options)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        entryProvider = { route ->
            when (route) {
                DeveloperDestination.Options -> NavEntry(route) {
                    DeveloperOptionsScreen(
                        onAction = { action ->
                            when (action) {
                                DeveloperOptionAction.OpenTechnicalDemo -> {
                                    backStack.add(DeveloperDestination.TechnicalDemo)
                                }

                                DeveloperOptionAction.ResetOnboarding -> onResetOnboarding()
                            }
                        },
                    )
                }

                DeveloperDestination.TechnicalDemo -> NavEntry(route) {
                    DemoScreen()
                }

                else -> error("Unknown developer route: $route")
            }
        },
    )
}

@Serializable
private sealed interface DeveloperDestination : NavKey {
    @Serializable
    data object Options : DeveloperDestination

    @Serializable
    data object TechnicalDemo : DeveloperDestination
}

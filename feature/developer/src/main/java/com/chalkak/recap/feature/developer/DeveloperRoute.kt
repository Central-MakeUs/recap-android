package com.chalkak.recap.feature.developer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.chalkak.recap.core.design.animation.RecapNavDisplay
import com.chalkak.recap.core.design.animation.RecapNavigationMotion
import kotlinx.serialization.Serializable

@Composable
fun DeveloperRoute(
    onResetOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeveloperViewModel = hiltViewModel(),
) {
    val backStack = rememberNavBackStack(DeveloperDestination.Options)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RecapNavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        transitionSpec = { RecapNavigationMotion.forward() },
        popTransitionSpec = { RecapNavigationMotion.pop() },
        entryProvider = { route ->
            when (route) {
                DeveloperDestination.Options -> NavEntry(route) {
                    DeveloperOptionsScreen(
                        uiState = uiState,
                        onAction = { action ->
                            when (action) {
                                DeveloperOptionAction.OpenComponentGarden -> {
                                    backStack.add(DeveloperDestination.ComponentGarden)
                                }

                                DeveloperOptionAction.ResetOnboarding -> onResetOnboarding()
                                else -> viewModel.onAction(action)
                            }
                        },
                    )
                }

                DeveloperDestination.ComponentGarden -> NavEntry(route) {
                    ComponentGardenScreen()
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
    data object ComponentGarden : DeveloperDestination
}

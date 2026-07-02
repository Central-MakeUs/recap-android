package com.chalkak.recap.feature.developer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    viewModel: DeveloperViewModel = hiltViewModel(),
) {
    val backStack = rememberNavBackStack(DeveloperDestination.Options)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        entryProvider = { route ->
            when (route) {
                DeveloperDestination.Options -> NavEntry(route) {
                    val viewModel: DeveloperOptionsViewModel = hiltViewModel()
                    val modelDownloadState by viewModel.modelDownloadState.collectAsStateWithLifecycle()

                    DeveloperOptionsScreen(
                        ocrRawResults = uiState.ocrRawResults,
                        modelDownloadState = modelDownloadState,
                        onAction = { action ->
                            when (action) {
                                DeveloperOptionAction.OpenTechnicalDemo -> {
                                    backStack.add(DeveloperDestination.TechnicalDemo)
                                }

                                DeveloperOptionAction.OpenComponentGarden -> {
                                    backStack.add(DeveloperDestination.ComponentGarden)
                                }

                                DeveloperOptionAction.ResetOnboarding -> onResetOnboarding()
                                DeveloperOptionAction.DownloadEntityExtractionModel -> {
                                    viewModel.downloadEntityExtractionModel()
                                }
                            }
                        },
                    )
                }

                DeveloperDestination.TechnicalDemo -> NavEntry(route) {
                    DemoScreen()
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
    data object TechnicalDemo : DeveloperDestination

    @Serializable
    data object ComponentGarden : DeveloperDestination
}

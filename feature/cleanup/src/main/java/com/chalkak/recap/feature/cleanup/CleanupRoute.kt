package com.chalkak.recap.feature.cleanup

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.chalkak.recap.core.design.R
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Composable
fun CleanupRoute(
    onNavigateBack: () -> Unit,
    onCleanupComplete: () -> Unit,
    viewModel: CleanupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val cleanupBackStack = rememberNavBackStack(CleanupDestination.Selection)
    val currentDestination = cleanupBackStack.lastOrNull() as? CleanupDestination
        ?: CleanupDestination.Selection
    val maxSelectionMessage = stringResource(
        R.string.cleanup_max_selection_message,
        MAX_SELECTION_COUNT,
    )
    val startPlaceholderMessage = stringResource(R.string.cleanup_start_placeholder_message)

    LaunchedEffect(uiState.showMaxSelectionReached) {
        if (uiState.showMaxSelectionReached) {
            snackbarHostState.showSnackbar(maxSelectionMessage)
            viewModel.onAction(CleanupAction.DismissMaxSelectionMessage)
        }
    }

    LaunchedEffect(uiState.selectedUris, currentDestination) {
        if (currentDestination == CleanupDestination.Confirmation && uiState.selectedUris.isEmpty()) {
            cleanupBackStack.removeLastOrNull()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { _ ->
        NavDisplay(
            backStack = cleanupBackStack,
            onBack = {
                if (cleanupBackStack.size > 1) {
                    cleanupBackStack.removeLastOrNull()
                } else {
                    onNavigateBack()
                }
            },
            entryProvider = { destination ->
                when (destination) {
                    CleanupDestination.Selection -> NavEntry(destination) {
                        ScreenshotSelectionScreen(
                            uiState = uiState,
                            onAction = viewModel::onAction,
                            onCancelClick = onNavigateBack,
                            onNextClick = {
                                if (uiState.canProceed) {
                                    cleanupBackStack.add(CleanupDestination.Confirmation)
                                }
                            },
                        )
                    }

                    CleanupDestination.Confirmation -> NavEntry(destination) {
                        ScreenshotConfirmationScreen(
                            uiState = uiState,
                            onAction = viewModel::onAction,
                            onBackClick = { cleanupBackStack.removeLastOrNull() },
                            onAddMoreClick = { cleanupBackStack.removeLastOrNull() },
                            onStartOrganizingClick = {
                                if (uiState.canProceed) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(startPlaceholderMessage)
                                        onCleanupComplete()
                                    }
                                }
                            },
                        )
                    }

                    else -> error("Unknown cleanup destination: $destination")
                }
            },
        )
    }
}

@Serializable
private sealed interface CleanupDestination : NavKey {
    @Serializable
    data object Selection : CleanupDestination

    @Serializable
    data object Confirmation : CleanupDestination
}

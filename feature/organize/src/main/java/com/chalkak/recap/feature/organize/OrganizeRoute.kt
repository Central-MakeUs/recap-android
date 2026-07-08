package com.chalkak.recap.feature.organize

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.model.LocalImage
import kotlinx.serialization.Serializable

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun OrganizeRoute(
    onNavigateBack: () -> Unit,
    onOrganizeComplete: (List<LocalImage>) -> Unit,
    viewModel: OrganizeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val organizeBackStack = rememberNavBackStack(OrganizeDestination.Selection)
    val currentDestination = organizeBackStack.lastOrNull() as? OrganizeDestination
        ?: OrganizeDestination.Selection
    val maxSelectionMessage = stringResource(
        R.string.organize_max_selection_message,
        MAX_SELECTION_COUNT,
    )
    LaunchedEffect(uiState.showMaxSelectionReached) {
        if (uiState.showMaxSelectionReached) {
            snackbarHostState.showSnackbar(maxSelectionMessage)
            viewModel.onAction(OrganizeAction.DismissMaxSelectionMessage)
        }
    }

    LaunchedEffect(uiState.selectedUris, currentDestination) {
        if (currentDestination == OrganizeDestination.Confirmation && uiState.selectedUris.isEmpty()) {
            organizeBackStack.removeLastOrNull()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { _ ->
        NavDisplay(
            backStack = organizeBackStack,
            onBack = {
                if (organizeBackStack.size > 1) {
                    organizeBackStack.removeLastOrNull()
                } else {
                    onNavigateBack()
                }
            },
            entryProvider = { destination ->
                when (destination) {
                    OrganizeDestination.Selection -> NavEntry(destination) {
                        ScreenshotSelectionScreen(
                            uiState = uiState,
                            onAction = viewModel::onAction,
                            onCancelClick = onNavigateBack,
                            onNextClick = {
                                if (uiState.canProceed) {
                                    organizeBackStack.add(OrganizeDestination.Confirmation)
                                }
                            },
                        )
                    }

                    OrganizeDestination.Confirmation -> NavEntry(destination) {
                        ScreenshotConfirmationScreen(
                            uiState = uiState,
                            onAction = viewModel::onAction,
                            onBackClick = { organizeBackStack.removeLastOrNull() },
                            onAddMoreClick = { organizeBackStack.removeLastOrNull() },
                            onStartOrganizingClick = {
                                if (uiState.canProceed) {
                                    val selectedScreenshots = uiState.availableScreenshots
                                        .filter { screenshot -> screenshot.uri in uiState.selectedUris }
                                        .sortedBy { screenshot -> uiState.selectedUris.indexOf(screenshot.uri) }
                                    onOrganizeComplete(selectedScreenshots)
                                }
                            },
                        )
                    }

                    else -> error("Unknown organize destination: $destination")
                }
            },
        )
    }
}

@Serializable
private sealed interface OrganizeDestination : NavKey {
    @Serializable
    data object Selection : OrganizeDestination

    @Serializable
    data object Confirmation : OrganizeDestination
}

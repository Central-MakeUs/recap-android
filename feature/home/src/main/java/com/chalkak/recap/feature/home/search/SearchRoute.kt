package com.chalkak.recap.feature.home.search

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.toast.LocalRecapToastDispatcher
import com.chalkak.recap.core.design.component.toast.RecapToastType

@Composable
fun SearchRoute(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToScreenshot: (Long) -> Unit,
    onNavigateToScreenshotEdit: (Long) -> Unit = {},
    isCurrentDestination: Boolean = true,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var retainedUiState by remember { mutableStateOf(uiState) }
    val displayedUiState = if (isCurrentDestination) {
        SideEffect { retainedUiState = uiState }
        uiState
    } else {
        retainedUiState
    }
    val toastDispatcher = LocalRecapToastDispatcher.current
    val deleteSuccessToastMessage = stringResource(R.string.screenshot_delete_success_toast)
    val deleteFailureToastMessage = stringResource(R.string.screenshot_detail_delete_error)

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.onAction(SearchAction.LeaveComposition)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                SearchEvent.ShowDeleteSuccessToast -> toastDispatcher.showToast(
                    message = deleteSuccessToastMessage,
                    type = RecapToastType.Success,
                )

                SearchEvent.ShowDeleteFailureToast -> toastDispatcher.showToast(
                    message = deleteFailureToastMessage,
                    type = RecapToastType.Error,
                )
            }
        }
    }

    SearchScreen(
        modifier = modifier.fillMaxSize(),
        uiState = displayedUiState,
        onAction = { action ->
            when (action) {
                SearchAction.NavigateBack -> {
                    viewModel.onAction(action)
                    onNavigateBack()
                }
                is SearchAction.SelectResult -> {
                    viewModel.onAction(action)
                    onNavigateToScreenshot(action.captureId)
                }
                is SearchAction.EditResult -> {
                    viewModel.onAction(action)
                    onNavigateToScreenshotEdit(action.captureId)
                }
                else -> viewModel.onAction(action)
            }
        },
    )
}

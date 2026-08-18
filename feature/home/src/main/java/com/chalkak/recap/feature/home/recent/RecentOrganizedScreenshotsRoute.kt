package com.chalkak.recap.feature.home.recent

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.toast.LocalRecapToastDispatcher
import com.chalkak.recap.core.design.component.toast.RecapToastType

@Composable
fun RecentOrganizedScreenshotsRoute(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToScreenshot: (Long) -> Unit,
    onNavigateToOrganize: () -> Unit,
    onNavigateToScreenshotEdit: (Long) -> Unit = {},
    viewModel: RecentOrganizedScreenshotsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastDispatcher = LocalRecapToastDispatcher.current
    val deleteSuccessToastMessage = stringResource(R.string.screenshot_delete_success_toast)
    val deleteFailureToastMessage = stringResource(R.string.screenshot_detail_delete_error)

    DisposableEffect(viewModel) {
        viewModel.onListVisible()
        onDispose { viewModel.onListHidden() }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                RecentOrganizedScreenshotsEvent.ShowDeleteSuccessToast -> toastDispatcher.showToast(
                    message = deleteSuccessToastMessage,
                    type = RecapToastType.Success,
                )

                RecentOrganizedScreenshotsEvent.ShowDeleteFailureToast -> toastDispatcher.showToast(
                    message = deleteFailureToastMessage,
                    type = RecapToastType.Error,
                )
            }
        }
    }

    val currentOnNavigateBack by rememberUpdatedState(onNavigateBack)
    val currentOnNavigateToSearch by rememberUpdatedState(onNavigateToSearch)
    val currentOnNavigateToOrganize by rememberUpdatedState(onNavigateToOrganize)
    val currentOnNavigateToScreenshot by rememberUpdatedState(onNavigateToScreenshot)
    val currentOnNavigateToScreenshotEdit by rememberUpdatedState(onNavigateToScreenshotEdit)
    val onAction = remember(viewModel) {
        { action: RecentOrganizedScreenshotsAction ->
            when (action) {
                RecentOrganizedScreenshotsAction.NavigateBack -> currentOnNavigateBack()
                RecentOrganizedScreenshotsAction.OpenSearch -> currentOnNavigateToSearch()
                RecentOrganizedScreenshotsAction.StartImport -> currentOnNavigateToOrganize()
                RecentOrganizedScreenshotsAction.LoadMore,
                RecentOrganizedScreenshotsAction.Retry,
                is RecentOrganizedScreenshotsAction.ToggleFavorite,
                is RecentOrganizedScreenshotsAction.RequestDeleteItem,
                RecentOrganizedScreenshotsAction.ConfirmDeleteItem,
                RecentOrganizedScreenshotsAction.DismissDeleteItem,
                -> viewModel.onAction(action)

                is RecentOrganizedScreenshotsAction.SelectItem ->
                    currentOnNavigateToScreenshot(action.id)

                is RecentOrganizedScreenshotsAction.EditItem ->
                    currentOnNavigateToScreenshotEdit(action.id)
            }
        }
    }

    RecentOrganizedScreenshotsScreen(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onAction = onAction,
    )
}

package com.chalkak.recap.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.toast.RecapToastHost
import com.chalkak.recap.core.design.component.toast.RecapToastType
import com.chalkak.recap.core.design.component.toast.rememberRecapToastHostState
import dev.chrisbanes.haze.HazePositionStrategy
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun RecentOrganizedScreenshotsRoute(
    onNavigateBack: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToScreenshot: (String) -> Unit,
    onNavigateToOrganize: () -> Unit,
    modifier: Modifier = Modifier,
    pendingScreenshotDeletedToast: Boolean = false,
    onPendingScreenshotDeletedToastConsumed: () -> Unit = {},
    viewModel: RecentOrganizedScreenshotsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hazeState = rememberHazeState(positionStrategy = HazePositionStrategy.Screen)
    val toastHostState = rememberRecapToastHostState()
    val resources = LocalResources.current
    val navigationBarBottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    LaunchedEffect(pendingScreenshotDeletedToast) {
        if (!pendingScreenshotDeletedToast) return@LaunchedEffect
        onPendingScreenshotDeletedToastConsumed()
        toastHostState.showToast(
            message = resources.getString(R.string.screenshot_delete_success_toast),
            type = RecapToastType.Success,
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        RecentOrganizedScreenshotsScreen(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            uiState = uiState,
            onAction = { action ->
                when (action) {
                    RecentOrganizedScreenshotsAction.NavigateBack -> onNavigateBack()
                    RecentOrganizedScreenshotsAction.OpenSearch -> onNavigateToSearch()
                    RecentOrganizedScreenshotsAction.StartImport -> onNavigateToOrganize()
                    is RecentOrganizedScreenshotsAction.ToggleFavorite -> viewModel.onAction(action)
                    is RecentOrganizedScreenshotsAction.SelectItem -> onNavigateToScreenshot(action.id)
                }
            },
        )

        RecapToastHost(
            hostState = toastHostState,
            hazeState = hazeState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = navigationBarBottomPadding + 8.dp),
        )
    }
}

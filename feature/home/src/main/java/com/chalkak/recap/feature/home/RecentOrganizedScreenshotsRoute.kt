package com.chalkak.recap.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RecentOrganizedScreenshotsRoute(
    onNavigateBack: () -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecentOrganizedScreenshotsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RecentOrganizedScreenshotsScreen(
        modifier = modifier,
        uiState = uiState,
        onAction = { action ->
            when (action) {
                RecentOrganizedScreenshotsAction.NavigateBack -> onNavigateBack()
                RecentOrganizedScreenshotsAction.OpenSearch -> onNavigateToSearch()
                is RecentOrganizedScreenshotsAction.ToggleFavorite -> viewModel.onAction(action)
                is RecentOrganizedScreenshotsAction.SelectItem -> Unit
            }
        },
    )
}

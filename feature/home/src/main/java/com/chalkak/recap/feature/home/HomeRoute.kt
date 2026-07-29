package com.chalkak.recap.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisbanes.haze.HazeState

@Composable
fun HomeRoute(
    hazeState: HazeState,
    onNavigateToDeveloper: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToRecentOrganizedScreenshots: () -> Unit,
    onNavigateToCollectionFavorites: () -> Unit,
    onNavigateToCollectionTypeDetail: (String) -> Unit,
    onNavigateToOrganize: () -> Unit,
    onNavigateToScreenshot: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    showDeveloperLogoShortcut: Boolean = false,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        hazeState = hazeState,
        uiState = uiState,
        onLogoClick = onNavigateToDeveloper.takeIf { showDeveloperLogoShortcut },
        onAction = { action ->
            when (action) {
                HomeAction.StartImport -> onNavigateToOrganize()
                HomeAction.EnterDeveloperOptions -> onNavigateToDeveloper()
                HomeAction.OpenSettings -> onNavigateToSettings()
                HomeAction.OpenSearch -> onNavigateToSearch()
                HomeAction.OpenRecentScreenshots -> onNavigateToRecentOrganizedScreenshots()
                HomeAction.OpenFavoriteCategories -> onNavigateToCollectionFavorites()
                is HomeAction.ToggleFavoriteItem -> viewModel.onAction(action)
                HomeAction.RetryLoad -> viewModel.onAction(action)
                is HomeAction.SelectRecentScreenshot -> onNavigateToScreenshot(action.id)
                is HomeAction.SelectFavoriteItem -> onNavigateToScreenshot(action.id)
                HomeAction.OpenFrequentSaveTypes -> Unit
                is HomeAction.SelectFrequentSaveType ->
                    onNavigateToCollectionTypeDetail(action.contentTypeName)
            }
        },
    )
}

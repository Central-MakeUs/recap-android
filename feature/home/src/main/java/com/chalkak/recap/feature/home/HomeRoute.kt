package com.chalkak.recap.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun HomeRoute(
    hazeState: HazeState,
    onNavigateToDeveloper: () -> Unit,
    onNavigateToMyPage: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToRecentOrganizedScreenshots: () -> Unit,
    onNavigateToCollectionFavorites: () -> Unit,
    modifier: Modifier = Modifier,
    analysisProgressFlow: Flow<HomeAnalysisProgressUiModel> = flowOf(HomeAnalysisProgressUiModel()),
    viewModel: HomeViewModel = hiltViewModel(),
    showDeveloperLogoShortcut: Boolean = false,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val analysisProgress by analysisProgressFlow.collectAsStateWithLifecycle(
        initialValue = HomeAnalysisProgressUiModel(),
    )

    HomeScreen(
        modifier = modifier,
        hazeState = hazeState,
        uiState = uiState,
        analysisProgress = analysisProgress,
        onLogoClick = onNavigateToDeveloper.takeIf { showDeveloperLogoShortcut },
        onAction = { action ->
            when (action) {
                HomeAction.StartImport -> Unit
                HomeAction.EnterDeveloperOptions -> onNavigateToDeveloper()
                HomeAction.OpenSettings -> onNavigateToMyPage()
                HomeAction.OpenSearch -> onNavigateToSearch()
                HomeAction.OpenRecentScreenshots -> onNavigateToRecentOrganizedScreenshots()
                HomeAction.OpenFavoriteCategories -> onNavigateToCollectionFavorites()
                is HomeAction.ToggleFavoriteItem -> viewModel.onAction(action)
                // TODO: Connect remaining home card actions when destinations are defined.
                is HomeAction.SelectRecentScreenshot -> Unit
                is HomeAction.SelectFavoriteItem -> Unit
                HomeAction.OpenFrequentSaveTypes -> Unit
                is HomeAction.SelectFrequentSaveType -> Unit
            }
        },
    )
}

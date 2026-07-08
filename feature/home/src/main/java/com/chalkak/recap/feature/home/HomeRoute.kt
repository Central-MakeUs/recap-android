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
    modifier: Modifier = Modifier,
    analysisProgressFlow: Flow<HomeAnalysisProgressUiModel> = flowOf(HomeAnalysisProgressUiModel()),
    viewModel: HomeViewModel = hiltViewModel(),
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
        onAction = { action ->
            when (action) {
                HomeAction.StartImport -> Unit
                HomeAction.EnterDeveloperOptions -> onNavigateToDeveloper()
                // TODO: Connect home mock card actions when destinations are defined.
                HomeAction.OpenRecentScreenshots -> Unit
                is HomeAction.SelectRecentScreenshot -> Unit
                HomeAction.OpenFavoriteCategories -> Unit
                is HomeAction.SelectFavoriteCategory -> Unit
                is HomeAction.ToggleFavoriteCategory -> Unit
                HomeAction.OpenFrequentSaveTypes -> Unit
                is HomeAction.SelectFrequentSaveType -> Unit
            }
        },
    )
}

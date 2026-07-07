package com.chalkak.recap.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeRoute(
    onNavigateToDeveloper: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
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

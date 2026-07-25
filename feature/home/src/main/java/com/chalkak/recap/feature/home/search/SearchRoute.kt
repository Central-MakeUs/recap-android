package com.chalkak.recap.feature.home.search

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SearchRoute(
    onNavigateBack: () -> Unit,
    onNavigateToScreenshot: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearchScreen(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onAction = { action ->
            when (action) {
                SearchAction.NavigateBack -> onNavigateBack()
                is SearchAction.SelectResult -> onNavigateToScreenshot(action.captureId)
                else -> viewModel.onAction(action)
            }
        },
    )
}

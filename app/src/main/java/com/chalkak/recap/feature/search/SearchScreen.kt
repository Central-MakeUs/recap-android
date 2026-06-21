package com.chalkak.recap.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chalkak.recap.core.design.component.RecapPlaceholderScreen

@Composable
fun SearchScreen(
    uiState: SearchUiState = SearchUiState(),
    modifier: Modifier = Modifier,
    onAction: (SearchAction) -> Unit = {},
) {
    RecapPlaceholderScreen(
        title = uiState.title,
        description = uiState.description,
        modifier = modifier,
    )
}

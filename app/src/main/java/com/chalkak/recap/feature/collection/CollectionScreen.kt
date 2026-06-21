package com.chalkak.recap.feature.collection

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chalkak.recap.core.design.component.RecapPlaceholderScreen

@Composable
fun CollectionScreen(
    uiState: CollectionUiState = CollectionUiState(),
    modifier: Modifier = Modifier,
    onAction: (CollectionAction) -> Unit = {},
) {
    RecapPlaceholderScreen(
        title = uiState.title,
        description = uiState.description,
        modifier = modifier,
    )
}

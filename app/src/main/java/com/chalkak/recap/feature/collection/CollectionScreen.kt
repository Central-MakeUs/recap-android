package com.chalkak.recap.feature.collection

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.chalkak.recap.core.design.component.RecapPlaceholderScreen

@Composable
fun CollectionScreen(
    modifier: Modifier = Modifier,
    uiState: CollectionUiState = CollectionUiState(),
    onAction: (CollectionAction) -> Unit = {},
) {
    RecapPlaceholderScreen(
        title = stringResource(uiState.titleResId),
        description = stringResource(uiState.descriptionResId),
        modifier = modifier,
    )
}

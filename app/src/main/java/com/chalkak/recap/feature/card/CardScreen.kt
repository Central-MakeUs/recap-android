package com.chalkak.recap.feature.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chalkak.recap.core.design.component.RecapPlaceholderScreen

@Composable
fun CardScreen(
    uiState: CardUiState = CardUiState(),
    modifier: Modifier = Modifier,
    onAction: (CardAction) -> Unit = {},
) {
    RecapPlaceholderScreen(
        title = uiState.title,
        description = uiState.description,
        modifier = modifier,
    )
}

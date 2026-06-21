package com.chalkak.recap.feature.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chalkak.recap.core.design.component.RecapPlaceholderScreen

@Composable
fun DemoScreen(
    uiState: DemoUiState = DemoUiState(),
    modifier: Modifier = Modifier,
    onAction: (DemoAction) -> Unit = {},
) {
    RecapPlaceholderScreen(
        title = uiState.title,
        description = uiState.description,
        modifier = modifier,
    )
}

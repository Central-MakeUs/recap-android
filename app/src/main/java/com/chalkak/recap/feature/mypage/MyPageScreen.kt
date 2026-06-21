package com.chalkak.recap.feature.mypage

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chalkak.recap.core.design.component.RecapPlaceholderScreen

@Composable
fun MyPageScreen(
    uiState: MyPageUiState = MyPageUiState(),
    modifier: Modifier = Modifier,
    onAction: (MyPageAction) -> Unit = {},
) {
    RecapPlaceholderScreen(
        title = uiState.title,
        description = uiState.description,
        modifier = modifier,
    )
}

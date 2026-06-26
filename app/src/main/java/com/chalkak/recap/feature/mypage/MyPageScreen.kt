package com.chalkak.recap.feature.mypage

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.chalkak.recap.core.design.component.RecapPlaceholderScreen

@Composable
fun MyPageScreen(
    uiState: MyPageUiState = MyPageUiState(),
    modifier: Modifier = Modifier,
    onAction: (MyPageAction) -> Unit = {},
) {
    RecapPlaceholderScreen(
        title = stringResource(uiState.titleResId),
        description = stringResource(uiState.descriptionResId),
        modifier = modifier,
    )
}

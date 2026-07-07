package com.chalkak.recap.feature.mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MyPageNotificationSettingsRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyPageNotificationSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MyPageNotificationSettingsScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                MyPageNotificationSettingsAction.NavigateBack -> onNavigateBack()
                else -> viewModel.onAction(action)
            }
        },
        modifier = modifier,
    )
}

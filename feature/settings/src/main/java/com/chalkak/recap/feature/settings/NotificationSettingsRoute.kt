package com.chalkak.recap.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chalkak.recap.feature.settings.screen.NotificationSettingsScreen

@Composable
fun NotificationSettingsRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NotificationSettingsScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                NotificationSettingsAction.NavigateBack -> onNavigateBack()
                else -> viewModel.onAction(action)
            }
        },
        modifier = modifier,
    )
}

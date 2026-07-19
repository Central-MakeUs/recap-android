package com.chalkak.recap.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chalkak.recap.feature.settings.screen.AccountManagementScreen

@Composable
fun AccountManagementRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountManagementViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadAccountInfo()
    }

    AccountManagementScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                AccountManagementAction.NavigateBack -> onNavigateBack()
                else -> viewModel.onAction(action)
            }
        },
        modifier = modifier,
    )
}

package com.chalkak.recap.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.toast.RecapToastType
import com.chalkak.recap.core.design.component.toast.rememberRecapToastHostState
import com.chalkak.recap.feature.settings.screen.DataManagementScreen

@Composable
fun DataManagementRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DataManagementViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastHostState = rememberRecapToastHostState()
    val resources = LocalResources.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is DataManagementEvent.ShowDeleteSuccessToast -> {
                    toastHostState.showToast(
                        message = resources.getString(
                            R.string.settings_data_management_delete_success_toast,
                            event.deletedCount,
                        ),
                        type = RecapToastType.Success,
                    )
                }
            }
        }
    }

    DataManagementScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                DataManagementAction.NavigateBack -> onNavigateBack()
                else -> viewModel.onAction(action)
            }
        },
        toastHostState = toastHostState,
        modifier = modifier,
    )
}

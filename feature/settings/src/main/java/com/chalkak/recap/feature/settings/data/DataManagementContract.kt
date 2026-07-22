package com.chalkak.recap.feature.settings.data

data class DataManagementUiState(
    val organizedCount: Int = 0,
    val showDeleteConfirmDialog: Boolean = false,
)

sealed interface DataManagementAction {
    data object NavigateBack : DataManagementAction
    data object DeleteDataClick : DataManagementAction
    data object DismissDeleteConfirmDialog : DataManagementAction
    data object ConfirmDeleteData : DataManagementAction
}

sealed interface DataManagementEvent {
    data class ShowDeleteSuccessToast(val deletedCount: Int) : DataManagementEvent
}

package com.chalkak.recap.feature.settings.data

data class DataManagementUiState(
    val organizedCount: Int = 0,
    val showDeleteConfirmDialog: Boolean = false,
    val showWithdrawConsentDialog: Boolean = false,
    val isAiDataTransferConsented: Boolean = false,
    val aiDataTransferConsentDate: String = "",
)

sealed interface DataManagementAction {
    data object NavigateBack : DataManagementAction
    data object DeleteDataClick : DataManagementAction
    data object DismissDeleteConfirmDialog : DataManagementAction
    data object ConfirmDeleteData : DataManagementAction
    data object AiDataTransferConsentClick : DataManagementAction
    data object DismissWithdrawConsentDialog : DataManagementAction
    data object ConfirmWithdrawConsent : DataManagementAction
}

sealed interface DataManagementEvent {
    data class ShowDeleteSuccessToast(val deletedCount: Int) : DataManagementEvent
    data object ShowConsentWithdrawnToast : DataManagementEvent
}

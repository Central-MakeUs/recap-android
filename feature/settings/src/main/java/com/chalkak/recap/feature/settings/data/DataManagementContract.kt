package com.chalkak.recap.feature.settings.data

data class DataManagementUiState(
    val organizedCount: Int? = null,
    val showDeleteConfirmDialog: Boolean = false,
    val showWithdrawConsentDialog: Boolean = false,
    val showAiDataTransferConsentSheet: Boolean = false,
    val isAiDataTransferConsented: Boolean? = null,
    val aiDataTransferConsentDate: String = "",
    val hasFetchError: Boolean = false,
)

sealed interface DataManagementAction {
    data object NavigateBack : DataManagementAction
    data object DeleteDataClick : DataManagementAction
    data object DismissDeleteConfirmDialog : DataManagementAction
    data object ConfirmDeleteData : DataManagementAction
    data object AiDataTransferConsentClick : DataManagementAction
    data object AgreeAiDataTransferConsent : DataManagementAction
    data object DismissAiDataTransferConsent : DataManagementAction
    data object DismissWithdrawConsentDialog : DataManagementAction
    data object ConfirmWithdrawConsent : DataManagementAction
}

sealed interface DataManagementEvent {
    data class ShowDeleteSuccessToast(val deletedCount: Int) : DataManagementEvent
    data object ShowNoDataToDeleteToast : DataManagementEvent
    data object ShowConsentWithdrawnToast : DataManagementEvent
}

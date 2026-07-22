package com.chalkak.recap.feature.settings.account

data class AccountManagementUiState(
    val joinedDate: String = "",
    val dialog: AccountManagementDialog = AccountManagementDialog.None,
)

enum class AccountManagementDialog {
    None,
    Logout,
    Withdraw,
}

sealed interface AccountManagementAction {
    data object NavigateBack : AccountManagementAction
    data object LogoutClick : AccountManagementAction
    data object WithdrawClick : AccountManagementAction
    data object DismissDialog : AccountManagementAction
    data object ConfirmLogout : AccountManagementAction
    data object ConfirmWithdraw : AccountManagementAction
}

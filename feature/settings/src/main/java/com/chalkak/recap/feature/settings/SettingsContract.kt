package com.chalkak.recap.feature.settings

import com.chalkak.recap.core.model.ImageAccessLevel

data class SettingsUiState(
    val photoAccessLevel: ImageAccessLevel = ImageAccessLevel.Denied,
)

sealed interface SettingsAction {
    data object NavigateBack : SettingsAction
    data object OpenAccountManagement : SettingsAction
    data object OpenNotificationSettings : SettingsAction
    data object OpenPhotoAccessPermission : SettingsAction
    data object OpenDataManagement : SettingsAction
    data object OpenUsageGuide : SettingsAction
    data object OpenPrivacyGuide : SettingsAction
    data object OpenContact : SettingsAction
    data object OpenOpenSourceLicenses : SettingsAction
}

data class NotificationSettingsUiState(
    val deviceNotificationsEnabled: Boolean = true,
    val organizeCompleteEnabled: Boolean = true,
)

sealed interface NotificationSettingsAction {
    data object NavigateBack : NotificationSettingsAction
    data object OpenDeviceNotificationSettings : NotificationSettingsAction
    data class OrganizeCompleteEnabledChanged(
        val enabled: Boolean,
    ) : NotificationSettingsAction
}

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

package com.chalkak.recap.feature.settings.notification

data class NotificationSettingsUiState(
    val deviceNotificationsEnabled: Boolean = true,
    val organizeCompleteNotificationEnabled: Boolean = false,
)

sealed interface NotificationSettingsAction {
    data object NavigateBack : NotificationSettingsAction
    data object RequestDeviceNotificationPermission : NotificationSettingsAction
    data class OrganizeCompleteNotificationEnabledChanged(
        val enabled: Boolean,
    ) : NotificationSettingsAction
}

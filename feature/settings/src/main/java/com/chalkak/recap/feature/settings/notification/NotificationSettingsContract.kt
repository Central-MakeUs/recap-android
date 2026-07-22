package com.chalkak.recap.feature.settings.notification

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

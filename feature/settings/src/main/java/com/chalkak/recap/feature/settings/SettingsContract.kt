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
    val organizeCompleteEnabled: Boolean = true,
    val reviewRequiredEnabled: Boolean = true,
    val marketingEnabled: Boolean = false,
)

sealed interface NotificationSettingsAction {
    data object NavigateBack : NotificationSettingsAction
    data class OrganizeCompleteEnabledChanged(
        val enabled: Boolean,
    ) : NotificationSettingsAction
    data class ReviewRequiredEnabledChanged(
        val enabled: Boolean,
    ) : NotificationSettingsAction
    data class MarketingEnabledChanged(
        val enabled: Boolean,
    ) : NotificationSettingsAction
}

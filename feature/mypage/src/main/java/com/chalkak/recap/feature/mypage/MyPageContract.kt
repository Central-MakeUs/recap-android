package com.chalkak.recap.feature.mypage

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

data class MyPageNotificationSettingsUiState(
    val organizeCompleteEnabled: Boolean = true,
    val reviewRequiredEnabled: Boolean = true,
    val marketingEnabled: Boolean = false,
)

sealed interface MyPageNotificationSettingsAction {
    data object NavigateBack : MyPageNotificationSettingsAction
    data class OrganizeCompleteEnabledChanged(
        val enabled: Boolean,
    ) : MyPageNotificationSettingsAction
    data class ReviewRequiredEnabledChanged(
        val enabled: Boolean,
    ) : MyPageNotificationSettingsAction
    data class MarketingEnabledChanged(
        val enabled: Boolean,
    ) : MyPageNotificationSettingsAction
}

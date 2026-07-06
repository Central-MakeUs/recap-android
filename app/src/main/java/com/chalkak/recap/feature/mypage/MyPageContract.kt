package com.chalkak.recap.feature.mypage

import androidx.annotation.StringRes
import com.chalkak.recap.R

data class MyPageUiState(
    @get:StringRes val titleResId: Int = R.string.my_page_title,
    @get:StringRes val descriptionResId: Int = R.string.my_page_description,
    val isLoggedIn: Boolean = true,
)

sealed interface MyPageAction {
    data object NavigateBack : MyPageAction
    data object Login : MyPageAction
    data object OpenNotificationSettings : MyPageAction
    data object OpenUploadGuide : MyPageAction
    data object OpenDataManagement : MyPageAction
    data object OpenPrivacyGuide : MyPageAction
    data object OpenServiceInfo : MyPageAction
}

data class MyPageNotificationSettingsUiState(
    val cleanupCompleteEnabled: Boolean = true,
    val reviewRequiredEnabled: Boolean = true,
    val marketingEnabled: Boolean = false,
)

sealed interface MyPageNotificationSettingsAction {
    data object NavigateBack : MyPageNotificationSettingsAction
    data class CleanupCompleteEnabledChanged(
        val enabled: Boolean,
    ) : MyPageNotificationSettingsAction
    data class ReviewRequiredEnabledChanged(
        val enabled: Boolean,
    ) : MyPageNotificationSettingsAction
    data class MarketingEnabledChanged(
        val enabled: Boolean,
    ) : MyPageNotificationSettingsAction
}

package com.chalkak.recap.feature.settings.notification

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.core.design.theme.RECAPTheme

@PreviewTest
@QaPhoneMatrix
@Composable
fun NotificationSettingsScreenScreenshot() {
    RECAPTheme(dynamicColor = false) {
        NotificationSettingsScreen(
            uiState = NotificationSettingsUiState(),
            onAction = {},
        )
    }
}

@PreviewTest
@QaPhoneMatrix
@Composable
fun NotificationSettingsScreenDeviceOffScreenshot() {
    RECAPTheme(dynamicColor = false) {
        NotificationSettingsScreen(
            uiState = NotificationSettingsUiState(deviceNotificationsEnabled = false),
            onAction = {},
        )
    }
}

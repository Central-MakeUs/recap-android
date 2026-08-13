package com.chalkak.recap.feature.settings

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.model.ImageAccessLevel

@PreviewTest
@QaPhoneMatrix
@Composable
fun SettingsScreenAllowedScreenshot() {
    RECAPTheme(dynamicColor = false) {
        SettingsScreen(
            uiState = SettingsUiState(photoAccessLevel = ImageAccessLevel.Full),
        )
    }
}

@PreviewTest
@QaPhoneMatrix
@Composable
fun SettingsScreenDeniedScreenshot() {
    RECAPTheme(dynamicColor = false) {
        SettingsScreen(
            uiState = SettingsUiState(photoAccessLevel = ImageAccessLevel.Denied),
        )
    }
}

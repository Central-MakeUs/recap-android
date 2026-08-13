package com.chalkak.recap.feature.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.core.design.theme.RECAPTheme

@PreviewTest
@QaPhoneMatrix
@Composable
fun ScreenshotFullscreenScreenScreenshot() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotFullscreenScreen(
            imageModel = null,
            onNavigateBack = {},
        )
    }
}

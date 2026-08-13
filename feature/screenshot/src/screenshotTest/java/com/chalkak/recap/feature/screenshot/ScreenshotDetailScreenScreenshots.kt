package com.chalkak.recap.feature.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.core.design.theme.RECAPTheme

@PreviewTest
@QaPhoneMatrix
@Composable
fun ScreenshotDetailContentScreenshot() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotDetailScreen(
            uiState = previewScreenshotContent(),
            onAction = {},
            onNavigateBack = {},
            onOpenEdit = {},
            onOpenFullscreen = {},
            onOpenMore = {},
        )
    }
}

@PreviewTest
@QaPhoneMatrix
@Composable
fun ScreenshotDetailLoadingScreenshot() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotDetailScreen(
            uiState = ScreenshotUiState.Loading,
            onAction = {},
            onNavigateBack = {},
            onOpenEdit = {},
            onOpenFullscreen = {},
            onOpenMore = {},
        )
    }
}

@PreviewTest
@QaPhoneMatrix
@Composable
fun ScreenshotDetailErrorScreenshot() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotDetailScreen(
            uiState = ScreenshotUiState.LoadError(),
            onAction = {},
            onNavigateBack = {},
            onOpenEdit = {},
            onOpenFullscreen = {},
            onOpenMore = {},
        )
    }
}

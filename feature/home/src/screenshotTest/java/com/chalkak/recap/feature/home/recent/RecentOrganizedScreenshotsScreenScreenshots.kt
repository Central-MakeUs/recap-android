package com.chalkak.recap.feature.home.recent

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.core.design.theme.RECAPTheme

@PreviewTest
@QaPhoneMatrix
@Composable
fun RecentOrganizedScreenshotsEmptyScreenshot() {
    RECAPTheme(dynamicColor = false) {
        RecentOrganizedScreenshotsScreen(
            uiState = RecentOrganizedScreenshotsUiState(
                phase = RecentOrganizedScreenshotsPhase.Empty,
            ),
            onAction = {},
        )
    }
}

@PreviewTest
@QaPhoneMatrix
@Composable
fun RecentOrganizedScreenshotsErrorScreenshot() {
    RECAPTheme(dynamicColor = false) {
        RecentOrganizedScreenshotsScreen(
            uiState = RecentOrganizedScreenshotsUiState(
                phase = RecentOrganizedScreenshotsPhase.Error,
            ),
            onAction = {},
        )
    }
}

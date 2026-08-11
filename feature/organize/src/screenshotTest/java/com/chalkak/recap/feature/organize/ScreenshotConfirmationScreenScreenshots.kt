package com.chalkak.recap.feature.organize

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.core.design.theme.RECAPTheme

@PreviewTest
@QaPhoneMatrix
@Composable
fun ScreenshotConfirmationScreenScreenshot() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotConfirmationScreen(
            uiState = OrganizeUiState(
                isLoading = false,
                availableScreenshots = OrganizeConfirmationPreviewScreenshots,
                selectedUris = OrganizeConfirmationPreviewScreenshots.map { it.uri },
            ),
            onAction = {},
            onBackClick = {},
            onAddMoreClick = {},
            onStartOrganizingClick = {},
        )
    }
}

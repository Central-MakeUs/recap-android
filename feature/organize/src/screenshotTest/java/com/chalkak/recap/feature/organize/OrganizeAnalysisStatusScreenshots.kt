package com.chalkak.recap.feature.organize

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.core.design.theme.RECAPTheme

@PreviewTest
@QaPhoneMatrix
@Composable
fun OrganizeAnalysisStatusProgressScreenshot() {
    RECAPTheme(dynamicColor = false) {
        OrganizeAnalysisStatusRoute(
            uiState = OrganizeAnalysisStatusUiState.Progress(progress = 0.65f),
            onCancelClick = {},
            onDismissClick = {},
            notificationsEnabled = true,
        )
    }
}

@PreviewTest
@QaPhoneMatrix
@Composable
fun OrganizeAnalysisStatusSuccessScreenshot() {
    RECAPTheme(dynamicColor = false) {
        OrganizeAnalysisStatusRoute(
            uiState = OrganizeAnalysisStatusUiState.Success(successCount = 5),
            onCancelClick = {},
            onDismissClick = {},
        )
    }
}

@PreviewTest
@QaPhoneMatrix
@Composable
fun OrganizeAnalysisStatusFailedScreenshot() {
    RECAPTheme(dynamicColor = false) {
        OrganizeAnalysisStatusRoute(
            uiState = OrganizeAnalysisStatusUiState.Failed,
            onCancelClick = {},
            onDismissClick = {},
        )
    }
}

@PreviewTest
@QaPhoneMatrix
@Composable
fun OrganizeAnalysisStatusPartialFailedScreenshot() {
    RECAPTheme(dynamicColor = false) {
        OrganizeAnalysisStatusRoute(
            uiState = OrganizeAnalysisStatusUiState.PartialFailed(successCount = 3),
            onCancelClick = {},
            onDismissClick = {},
        )
    }
}

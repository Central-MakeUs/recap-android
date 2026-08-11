package com.chalkak.recap.feature.organize

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.core.design.theme.RECAPTheme

@OptIn(ExperimentalMaterial3Api::class)
@PreviewTest
@QaPhoneMatrix
@Composable
fun ScreenshotPickerEmptyScreenshot() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotPickerContent(
            uiState = OrganizeUiState(isLoading = false),
            onAction = {},
            onCloseClick = {},
            onConfirmClick = {},
        )
    }
}

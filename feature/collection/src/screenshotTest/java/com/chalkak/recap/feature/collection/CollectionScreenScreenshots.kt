package com.chalkak.recap.feature.collection

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.component.topbar.CollectionTypeViewMode
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.core.design.theme.RECAPTheme

@PreviewTest
@QaPhoneMatrix
@Composable
fun CollectionScreenEmptyScreenshot() {
    RECAPTheme(dynamicColor = false) {
        CollectionScreen(
            uiState = CollectionUiState(
                isLoading = false,
                hasStoredScreenshots = false,
            ),
            onAction = {},
            onNavigateToOrganize = {},
        )
    }
}

@PreviewTest
@QaPhoneMatrix
@Composable
fun CollectionScreenLoadErrorScreenshot() {
    RECAPTheme(dynamicColor = false) {
        CollectionScreen(
            uiState = CollectionUiState(
                isLoading = false,
                isLoadError = true,
            ),
            onAction = {},
            onNavigateToOrganize = {},
        )
    }
}

@PreviewTest
@QaPhoneMatrix
@Composable
fun CollectionScreenOverviewGridScreenshot() {
    RECAPTheme(dynamicColor = false) {
        CollectionScreen(
            uiState = previewOverviewUiState(CollectionTypeViewMode.Grid),
            onAction = {},
            onNavigateToOrganize = {},
        )
    }
}

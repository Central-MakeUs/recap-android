package com.chalkak.recap.feature.home

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.core.design.theme.RECAPTheme
import dev.chrisbanes.haze.rememberHazeState

@PreviewTest
@QaPhoneMatrix
@Composable
fun HomeScreenEmptyScreenshot() {
    RECAPTheme(dynamicColor = false) {
        HomeScreen(hazeState = rememberHazeState())
    }
}

@PreviewTest
@QaPhoneMatrix
@Composable
fun HomeScreenLoadErrorScreenshot() {
    RECAPTheme(dynamicColor = false) {
        HomeScreen(
            hazeState = rememberHazeState(),
            uiState = HomeUiState(phase = HomeContentPhase.Error),
        )
    }
}

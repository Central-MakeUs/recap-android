package com.chalkak.recap.feature.home.search

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.core.design.theme.RECAPTheme

@PreviewTest
@QaPhoneMatrix
@Composable
fun SearchScreenIdleScreenshot() {
    RECAPTheme(dynamicColor = false) {
        SearchScreen(
            uiState = SearchUiState(
                recentSearches = listOf("검색어", "검색어 01234", "검색검색검색"),
            ),
        )
    }
}

@PreviewTest
@QaPhoneMatrix
@Composable
fun SearchScreenEmptyScreenshot() {
    RECAPTheme(dynamicColor = false) {
        SearchScreen(
            uiState = SearchUiState(
                query = "없는검색어",
                phase = SearchContentPhase.Empty,
            ),
        )
    }
}

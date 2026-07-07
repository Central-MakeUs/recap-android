package com.chalkak.recap.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun SearchRoute(
    onNavigateBack: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var recentSearches by rememberSaveable {
        mutableStateOf(listOf("숙소 예약", "반품 절차", "파스타"))
    }

    SearchScreen(
        uiState = SearchUiState(
            query = query,
            recentSearches = recentSearches,
        ),
        onAction = { action ->
            when (action) {
                SearchAction.NavigateBack -> onNavigateBack()
                is SearchAction.UpdateQuery -> query = action.query
                is SearchAction.SelectRecentSearch -> query = action.term
                SearchAction.ClearAllRecentSearches -> recentSearches = emptyList()
            }
        },
    )
}

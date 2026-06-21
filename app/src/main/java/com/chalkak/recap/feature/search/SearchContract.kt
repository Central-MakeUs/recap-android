package com.chalkak.recap.feature.search

data class SearchUiState(
    val title: String = "Search",
    val description: String = "Card and collection search will be handled here.",
)

sealed interface SearchAction {
    data class QueryChanged(val query: String) : SearchAction
}

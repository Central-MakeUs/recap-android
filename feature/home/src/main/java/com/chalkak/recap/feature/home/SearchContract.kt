package com.chalkak.recap.feature.home

data class SearchUiState(
    val query: String = "",
    val recentSearches: List<String> = emptyList(),
)

sealed interface SearchAction {
    data object NavigateBack : SearchAction
    data class UpdateQuery(val query: String) : SearchAction
    data class SelectRecentSearch(val term: String) : SearchAction
    data object ClearAllRecentSearches : SearchAction
}

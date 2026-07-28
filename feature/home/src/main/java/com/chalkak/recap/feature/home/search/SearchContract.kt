package com.chalkak.recap.feature.home.search

import com.chalkak.recap.core.design.category.RecapCategoryType

enum class SearchContentPhase {
    Idle,
    Loading,
    Results,
    Empty,
    Error,
}

data class SearchResultItemUiModel(
    val captureId: Long,
    val thumbnailModel: Any?,
    val categoryType: RecapCategoryType,
    val title: String,
    val description: String,
    val titleHighlightRange: IntRange? = null,
    val descriptionHighlightRange: IntRange? = null,
    val organizedAtMillis: Long,
    val isFavorite: Boolean,
)

data class SearchUiState(
    val query: String = "",
    val submittedQuery: String = "",
    val recentSearches: List<String> = emptyList(),
    val phase: SearchContentPhase = SearchContentPhase.Idle,
    val results: List<SearchResultItemUiModel> = emptyList(),
    val resultCount: Long = 0L,
    val hasNext: Boolean = false,
    val nextPage: Int = 0,
    val isLoadingMore: Boolean = false,
)

sealed interface SearchAction {
    data object NavigateBack : SearchAction
    data class UpdateQuery(val query: String) : SearchAction
    data object SubmitSearch : SearchAction
    data object RetrySearch : SearchAction
    data object LoadMore : SearchAction
    data class SelectRecentSearch(val term: String) : SearchAction
    data class RemoveRecentSearch(val term: String) : SearchAction
    data object ClearAllRecentSearches : SearchAction
    data class SelectResult(val captureId: Long) : SearchAction
    data class ToggleFavorite(val captureId: Long) : SearchAction
}

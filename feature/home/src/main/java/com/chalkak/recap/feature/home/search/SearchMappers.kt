package com.chalkak.recap.feature.home.search

import com.chalkak.recap.core.design.category.toRecapCategoryType
import com.chalkak.recap.core.design.component.text.findFirstHighlightRange
import com.chalkak.recap.core.design.component.text.toPlainSearchText
import com.chalkak.recap.core.model.search.SearchPage
import com.chalkak.recap.core.model.search.SearchResult
import java.time.Instant

internal fun SearchPage.toSearchResultItems(): List<SearchResultItemUiModel> =
    items.map { result -> result.toUiModel() }

internal fun SearchResult.toUiModel(): SearchResultItemUiModel {
    return SearchResultItemUiModel(
        captureId = captureId,
        thumbnailModel = thumbnailUrl?.takeIf { it.isNotBlank() },
        categoryType = typeCode.toRecapCategoryType(),
        title = titleHighlighted.toPlainSearchText(),
        description = summaryHighlighted.toPlainSearchText(),
        titleHighlightRange = findFirstHighlightRange(titleHighlighted),
        descriptionHighlightRange = findFirstHighlightRange(summaryHighlighted),
        organizedAtMillis = organizedAtMillis(),
        isFavorite = isFavorite,
    )
}

private fun SearchResult.organizedAtMillis(): Long =
    runCatching { Instant.parse(organizedAt).toEpochMilli() }.getOrDefault(0L)

internal fun List<String>.withRecentSearchTerm(term: String, limit: Int = 10): List<String> {
    val normalized = term.trim()
    if (normalized.isEmpty()) return this
    return (listOf(normalized) + filterNot { it.equals(normalized, ignoreCase = true) })
        .take(limit)
}

package com.chalkak.recap.core.model.search

import com.chalkak.recap.core.model.screenshot.ScreenshotContentType

enum class SearchScope {
    ALL,
    FAVORITE,
    ETC,
    TYPE,
}

data class SearchResult(
    val captureId: Long,
    val typeCode: ScreenshotContentType,
    val thumbnailUrl: String?,
    val titleHighlighted: String,
    val summaryHighlighted: String,
    val ocrExcerptHighlighted: String?,
    val isFavorite: Boolean,
    val organizedAt: String,
)

data class SearchPage(
    val count: Long,
    val hasNext: Boolean,
    val items: List<SearchResult>,
)

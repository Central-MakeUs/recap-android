package com.chalkak.recap.core.data.search

import com.chalkak.recap.core.data.capture.matchesSearch
import com.chalkak.recap.core.data.capture.sortedByOrganizedAt
import com.chalkak.recap.core.data.capture.toCaptureSummary
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.model.capture.CaptureSummary
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.search.SearchPage
import com.chalkak.recap.core.model.search.SearchResult
import com.chalkak.recap.core.model.search.SearchScope
import com.chalkak.recap.core.model.storage.CaptureSort
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class MockSearchRepository @Inject constructor(
    private val screenshotCardRepository: ScreenshotCardRepository,
) : SearchRepository {
    override fun observeSearch(
        query: String,
        scope: SearchScope,
        typeCode: ScreenshotContentType?,
        size: Int,
    ): Flow<Result<SearchPage>> =
        screenshotCardRepository.observeStoredCards().map { cards ->
            cards.map { it.toCaptureSummary() }
                .toSearchPage(
                    query = query,
                    scope = scope,
                    typeCode = typeCode,
                    page = 0,
                    size = size,
                )
        }

    override suspend fun search(
        query: String,
        scope: SearchScope,
        typeCode: ScreenshotContentType?,
        page: Int,
        size: Int,
    ): Result<SearchPage> {
        return screenshotCardRepository.observeStoredCards().first()
            .map { it.toCaptureSummary() }
            .toSearchPage(
                query = query,
                scope = scope,
                typeCode = typeCode,
                page = page,
                size = size,
            )
    }
}

private fun List<CaptureSummary>.toSearchPage(
    query: String,
    scope: SearchScope,
    typeCode: ScreenshotContentType?,
    page: Int,
    size: Int,
): Result<SearchPage> {
    val filtered = filterByScope(scope = scope, typeCode = typeCode)
        .matchesSearch(query)
        .sortedByOrganizedAt(CaptureSort.Latest)
    val safePage = page.coerceAtLeast(0)
    val safeSize = size.coerceAtLeast(1)
    val fromIndex = (safePage * safeSize).coerceAtMost(filtered.size)
    val toIndex = (fromIndex + safeSize).coerceAtMost(filtered.size)
    val pageItems = filtered.subList(fromIndex, toIndex)

    return Result.success(
        SearchPage(
            count = filtered.size.toLong(),
            hasNext = toIndex < filtered.size,
            items = pageItems.map { it.toSearchResult(query) },
        ),
    )
}

private fun List<CaptureSummary>.filterByScope(
    scope: SearchScope,
    typeCode: ScreenshotContentType?,
): List<CaptureSummary> =
    when (scope) {
        SearchScope.ALL -> this
        SearchScope.FAVORITE -> filter { it.isFavorite }
        SearchScope.ETC -> filter { it.typeCode == ScreenshotContentType.ETC }
        SearchScope.TYPE -> {
            if (typeCode == null) {
                emptyList()
            } else {
                filter { it.typeCode == typeCode }
            }
        }
    }

private fun CaptureSummary.toSearchResult(query: String): SearchResult =
    SearchResult(
        captureId = captureId,
        typeCode = typeCode,
        thumbnailUrl = thumbnailUrl,
        titleHighlighted = title.withFirstMark(query),
        summaryHighlighted = summary.withFirstMark(query),
        ocrExcerptHighlighted = null,
        isFavorite = isFavorite,
        organizedAt = organizedAt,
    )

private fun String.withFirstMark(query: String): String {
    val needle = query.trim()
    if (needle.isEmpty()) {
        return this
    }
    val index = indexOf(needle, ignoreCase = true)
    if (index < 0) {
        return this
    }
    val end = index + needle.length
    return substring(0, index) + "<mark>" + substring(index, end) + "</mark>" + substring(end)
}

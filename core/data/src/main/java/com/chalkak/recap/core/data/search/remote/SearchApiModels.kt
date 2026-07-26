package com.chalkak.recap.core.data.search.remote

import com.chalkak.recap.core.data.capture.remote.CardTypeDto
import com.chalkak.recap.core.data.capture.remote.toDomain
import com.chalkak.recap.core.data.network.ApiResponseDto
import com.chalkak.recap.core.model.search.SearchPage
import com.chalkak.recap.core.model.search.SearchResult
import kotlinx.serialization.Serializable

@Serializable
data class SearchResultResponseDto(
    val captureId: Long,
    val typeCode: CardTypeDto,
    val thumbnailUrl: String? = null,
    val titleHighlighted: String,
    val summaryHighlighted: String,
    val ocrExcerptHighlighted: String? = null,
    val isFavorite: Boolean,
    val organizedAt: String,
)

@Serializable
data class SearchResponseDto(
    val count: Long,
    val hasNext: Boolean,
    val items: List<SearchResultResponseDto> = emptyList(),
)

typealias SearchApiResponse = ApiResponseDto<SearchResponseDto>

fun SearchResultResponseDto.toDomain(): SearchResult =
    SearchResult(
        captureId = captureId,
        typeCode = typeCode.toDomain(),
        thumbnailUrl = thumbnailUrl,
        titleHighlighted = titleHighlighted,
        summaryHighlighted = summaryHighlighted,
        ocrExcerptHighlighted = ocrExcerptHighlighted,
        isFavorite = isFavorite,
        organizedAt = organizedAt,
    )

fun SearchResponseDto.toDomain(): SearchPage =
    SearchPage(
        count = count,
        hasNext = hasNext,
        items = items.map { it.toDomain() },
    )

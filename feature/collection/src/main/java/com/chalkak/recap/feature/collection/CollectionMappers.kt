package com.chalkak.recap.feature.collection

import com.chalkak.recap.core.design.category.toLabelResId
import com.chalkak.recap.core.design.category.toRecapCategoryType
import com.chalkak.recap.core.design.component.card.ScreenshotCardMetadataMode
import com.chalkak.recap.core.design.component.text.findFirstHighlightRange
import com.chalkak.recap.core.design.component.text.toPlainSearchText
import com.chalkak.recap.core.model.capture.CaptureList
import com.chalkak.recap.core.model.capture.CaptureSummary
import com.chalkak.recap.core.model.search.SearchResult
import com.chalkak.recap.core.model.storage.StorageOverview
import java.time.Instant

internal fun StorageOverview.toOverviewUiModel(): CollectionOverviewUiModel =
    CollectionOverviewUiModel(
        favoriteSummary = CollectionFavoriteSummaryUiModel(count = favoriteCount),
        typeSummaries = types.map { type ->
            val categoryType = type.typeCode.toRecapCategoryType()
            CollectionTypeSummaryUiModel(
                contentType = type.typeCode,
                labelResId = categoryType.labelResId,
                categoryType = categoryType,
                count = type.count.toInt(),
                exampleTitles = type.representativeTitles.take(2),
                additionalExampleCount = (type.count.toInt() - 2).coerceAtLeast(0),
            )
        },
    )

internal fun CaptureList.toDetailUiModel(
    filter: CollectionDetailFilter,
    sort: CollectionListSort,
    hasNext: Boolean = false,
    isLoadingMore: Boolean = false,
): CollectionDetailUiModel {
    val titleResId = when (filter) {
        is CollectionDetailFilter.ByType -> filter.contentType.toLabelResId()
        CollectionDetailFilter.Favorites ->
            com.chalkak.recap.core.design.R.string.collection_favorites_detail_title
    }
    val emptyMessageResId = when (filter) {
        CollectionDetailFilter.Favorites ->
            com.chalkak.recap.core.design.R.string.collection_favorites_empty
        is CollectionDetailFilter.ByType ->
            com.chalkak.recap.core.design.R.string.collection_detail_empty
    }
    val categoryType = when (filter) {
        is CollectionDetailFilter.ByType -> filter.contentType.toRecapCategoryType()
        CollectionDetailFilter.Favorites -> null
    }
    val cardMetadataMode = when (filter) {
        CollectionDetailFilter.Favorites -> ScreenshotCardMetadataMode.CategoryChip
        is CollectionDetailFilter.ByType -> ScreenshotCardMetadataMode.OrganizedDate
    }
    return CollectionDetailUiModel(
        titleResId = titleResId,
        count = count,
        sort = sort,
        cards = items.map { it.toCardItemUiModel() },
        emptyMessageResId = emptyMessageResId,
        categoryType = categoryType,
        cardMetadataMode = cardMetadataMode,
        hasNext = hasNext,
        isLoadingMore = isLoadingMore,
    )
}

internal fun CaptureSummary.toCardItemUiModel(): CollectionCardItemUiModel =
    CollectionCardItemUiModel(
        captureId = captureId,
        title = title,
        summary = summary,
        contentTypeLabelResId = typeCode.toLabelResId(),
        categoryType = typeCode.toRecapCategoryType(),
        organizedAtMillis = organizedAtMillis(),
        isFavorite = isFavorite,
        thumbnailModel = thumbnailModel(),
    )

internal fun CaptureSummary.thumbnailModel(): Any? = thumbnailUrl?.takeIf { it.isNotBlank() }

internal fun CaptureSummary.organizedAtMillis(): Long =
    runCatching { Instant.parse(organizedAt).toEpochMilli() }.getOrDefault(0L)

internal fun SearchResult.toCardItemUiModel(): CollectionCardItemUiModel =
    CollectionCardItemUiModel(
        captureId = captureId,
        title = titleHighlighted.toPlainSearchText(),
        summary = summaryHighlighted.toPlainSearchText(),
        contentTypeLabelResId = typeCode.toLabelResId(),
        categoryType = typeCode.toRecapCategoryType(),
        organizedAtMillis = runCatching {
            Instant.parse(organizedAt).toEpochMilli()
        }.getOrDefault(0L),
        isFavorite = isFavorite,
        thumbnailModel = thumbnailUrl?.takeIf { it.isNotBlank() },
        titleHighlightRange = findFirstHighlightRange(titleHighlighted),
        descriptionHighlightRange = findFirstHighlightRange(summaryHighlighted),
    )

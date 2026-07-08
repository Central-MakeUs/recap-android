package com.chalkak.recap.feature.collection

import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType

enum class CollectionListSort {
    Latest,
    Name,
}

data class CollectionCardItemUiModel(
    val imageId: String,
    val title: String,
    val summary: String,
    val contentTypeLabelResId: Int,
    val createdAtMillis: Long,
    val isFavorite: Boolean,
    val thumbnailModel: Any?,
)

data class CollectionTypeSummaryUiModel(
    val contentType: ScreenshotContentType,
    val labelResId: Int,
    val count: Int,
    val exampleTitles: List<String>,
    val additionalExampleCount: Int,
    val previewThumbnailModels: List<Any?>,
)

data class CollectionFavoriteSummaryUiModel(
    val count: Int,
    val previewThumbnailModels: List<Any?>,
)

data class CollectionOverviewUiModel(
    val favoriteSummary: CollectionFavoriteSummaryUiModel? = null,
    val typeSummaries: List<CollectionTypeSummaryUiModel> = emptyList(),
)

data class CollectionDetailUiModel(
    val titleResId: Int,
    val count: Int,
    val sort: CollectionListSort,
    val cards: List<CollectionCardItemUiModel>,
    val emptyMessageResId: Int,
)

data class CollectionUiState(
    val isLoading: Boolean = true,
    val hasStoredScreenshots: Boolean = false,
    val overview: CollectionOverviewUiModel = CollectionOverviewUiModel(),
    val detail: CollectionDetailUiModel? = null,
)

sealed interface CollectionAction {
    data object OpenFavoriteDetail : CollectionAction
    data class OpenTypeDetail(val contentType: ScreenshotContentType) : CollectionAction
    data object CloseDetail : CollectionAction
    data class SetDetailSort(val sort: CollectionListSort) : CollectionAction
    data class ToggleFavorite(val imageId: String) : CollectionAction
}

internal fun StoredScreenshotCard.toThumbnailModel(): Any? {
    return imageRefs.thumbnailPath ?: imageRefs.storedImagePath ?: imageRefs.sourceImageUri
}

internal fun StoredScreenshotCard.toCardItemUiModel(): CollectionCardItemUiModel {
    return CollectionCardItemUiModel(
        imageId = analysisResult.imageId,
        title = analysisResult.title,
        summary = analysisResult.summary,
        contentTypeLabelResId = analysisResult.contentTypes.primaryContentType.toLabelResId(),
        createdAtMillis = createdAtMillis,
        isFavorite = analysisResult.isFavorite,
        thumbnailModel = toThumbnailModel(),
    )
}

internal fun List<StoredScreenshotCard>.toOverviewUiModel(): CollectionOverviewUiModel {
    val sortedCards = sortedByDescending { card -> card.createdAtMillis }
    val favoriteCards = sortedCards.filter { card -> card.analysisResult.isFavorite }
    val favoriteSummary = if (favoriteCards.isNotEmpty()) {
        CollectionFavoriteSummaryUiModel(
            count = favoriteCards.size,
            previewThumbnailModels = favoriteCards.take(3).map(StoredScreenshotCard::toThumbnailModel),
        )
    } else {
        null
    }

    val typeSummaries = ScreenshotContentType.entries.mapNotNull { contentType ->
        val typeCards = sortedCards.filter { card ->
            card.analysisResult.contentTypes.primaryContentType == contentType
        }
        if (typeCards.isEmpty()) {
            return@mapNotNull null
        }
        CollectionTypeSummaryUiModel(
            contentType = contentType,
            labelResId = contentType.toLabelResId(),
            count = typeCards.size,
            exampleTitles = typeCards.map { card -> card.analysisResult.title }.take(2),
            additionalExampleCount = if (typeCards.size >= 3) typeCards.size - 2 else 0,
            previewThumbnailModels = typeCards.take(3).map(StoredScreenshotCard::toThumbnailModel),
        )
    }

    return CollectionOverviewUiModel(
        favoriteSummary = favoriteSummary,
        typeSummaries = typeSummaries,
    )
}

internal fun List<StoredScreenshotCard>.toDetailUiModel(
    filter: CollectionDetailFilter,
    sort: CollectionListSort,
): CollectionDetailUiModel {
    val filteredCards = when (filter) {
        is CollectionDetailFilter.ByType -> filter { card ->
            card.analysisResult.contentTypes.primaryContentType == filter.contentType
        }
        CollectionDetailFilter.Favorites -> filter { card -> card.analysisResult.isFavorite }
    }
    val sortedCards = when (sort) {
        CollectionListSort.Latest -> filteredCards.sortedByDescending { card -> card.createdAtMillis }
        CollectionListSort.Name -> filteredCards.sortedBy { card -> card.analysisResult.title }
    }
    val titleResId = when (filter) {
        is CollectionDetailFilter.ByType -> filter.contentType.toLabelResId()
        CollectionDetailFilter.Favorites -> com.chalkak.recap.core.design.R.string.collection_favorites_detail_title
    }
    val emptyMessageResId = when (filter) {
        CollectionDetailFilter.Favorites -> com.chalkak.recap.core.design.R.string.collection_favorites_empty
        is CollectionDetailFilter.ByType -> com.chalkak.recap.core.design.R.string.collection_detail_empty
    }
    return CollectionDetailUiModel(
        titleResId = titleResId,
        count = sortedCards.size,
        sort = sort,
        cards = sortedCards.map(StoredScreenshotCard::toCardItemUiModel),
        emptyMessageResId = emptyMessageResId,
    )
}

internal sealed interface CollectionDetailFilter {
    data class ByType(val contentType: ScreenshotContentType) : CollectionDetailFilter
    data object Favorites : CollectionDetailFilter
}

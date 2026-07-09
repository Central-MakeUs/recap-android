package com.chalkak.recap.feature.collection

import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.component.topbar.CollectionTypeViewMode
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType

enum class CollectionListSort {
    Latest,
    Name,
}

enum class CollectionTab {
    Favorites,
    Types,
    Others,
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

data class CollectionFavoriteItemUiModel(
    val imageId: String,
    val title: String,
    val summary: String,
    val categoryType: RecapCategoryType,
    val createdAtMillis: Long,
    val isFavorite: Boolean,
    val thumbnailModel: Any?,
)

data class CollectionTypeSummaryUiModel(
    val contentType: ScreenshotContentType,
    val labelResId: Int,
    val categoryType: RecapCategoryType?,
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
    val favoriteItems: List<CollectionFavoriteItemUiModel> = emptyList(),
    val typeSummaries: List<CollectionTypeSummaryUiModel> = emptyList(),
    val otherItems: List<CollectionCardItemUiModel> = emptyList(),
)

data class CollectionDetailUiModel(
    val titleResId: Int,
    val count: Int,
    val sort: CollectionListSort,
    val cards: List<CollectionCardItemUiModel>,
    val emptyMessageResId: Int,
    val categoryType: RecapCategoryType? = null,
)

data class CollectionUiState(
    val isLoading: Boolean = true,
    val hasStoredScreenshots: Boolean = false,
    val searchQuery: String = "",
    val selectedTab: CollectionTab = CollectionTab.Favorites,
    val typeViewMode: CollectionTypeViewMode = CollectionTypeViewMode.Grid,
    val othersSort: CollectionListSort = CollectionListSort.Latest,
    val overview: CollectionOverviewUiModel = CollectionOverviewUiModel(),
    val detail: CollectionDetailUiModel? = null,
)

sealed interface CollectionAction {
    data class UpdateSearchQuery(val query: String) : CollectionAction
    data class SelectTab(val tab: CollectionTab) : CollectionAction
    data class SetTypeViewMode(val viewMode: CollectionTypeViewMode) : CollectionAction
    data class SetOthersSort(val sort: CollectionListSort) : CollectionAction
    data object OpenFavoriteDetail : CollectionAction
    data class OpenFavoriteItem(val imageId: String) : CollectionAction
    data class OpenOtherItem(val imageId: String) : CollectionAction
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

internal fun StoredScreenshotCard.toFavoriteItemUiModel(): CollectionFavoriteItemUiModel? {
    val categoryType = analysisResult.contentTypes.primaryContentType.toRecapCategoryType()
        ?: return null
    return CollectionFavoriteItemUiModel(
        imageId = analysisResult.imageId,
        title = analysisResult.title,
        summary = analysisResult.summary,
        categoryType = categoryType,
        createdAtMillis = createdAtMillis,
        isFavorite = analysisResult.isFavorite,
        thumbnailModel = toThumbnailModel(),
    )
}

internal fun List<StoredScreenshotCard>.toOverviewUiModel(
    searchQuery: String = "",
    othersSort: CollectionListSort = CollectionListSort.Latest,
): CollectionOverviewUiModel {
    val normalizedQuery = searchQuery.trim()
    val sortedCards = sortedByDescending { card -> card.createdAtMillis }
        .filter { card ->
            if (normalizedQuery.isEmpty()) {
                true
            } else {
                card.analysisResult.title.contains(normalizedQuery, ignoreCase = true) ||
                    card.analysisResult.summary.contains(normalizedQuery, ignoreCase = true)
            }
        }
    val favoriteCards = sortedCards.filter { card -> card.analysisResult.isFavorite }
    val favoriteItems = favoriteCards.mapNotNull(StoredScreenshotCard::toFavoriteItemUiModel)
    val favoriteSummary = if (favoriteCards.isNotEmpty()) {
        CollectionFavoriteSummaryUiModel(
            count = favoriteCards.size,
            previewThumbnailModels = favoriteCards.take(3).map(StoredScreenshotCard::toThumbnailModel),
        )
    } else {
        null
    }

    val typeSummaries = ScreenshotContentType.entries
        .filter { contentType -> contentType != ScreenshotContentType.OTHER }
        .mapNotNull { contentType ->
            val typeCards = sortedCards.filter { card ->
                card.analysisResult.contentTypes.primaryContentType == contentType
            }
            if (typeCards.isEmpty()) {
                return@mapNotNull null
            }
            CollectionTypeSummaryUiModel(
                contentType = contentType,
                labelResId = contentType.toLabelResId(),
                categoryType = contentType.toRecapCategoryType(),
                count = typeCards.size,
                exampleTitles = typeCards.map { card -> card.analysisResult.title }.take(2),
                additionalExampleCount = if (typeCards.size >= 3) typeCards.size - 2 else 0,
                previewThumbnailModels = typeCards.take(3).map(StoredScreenshotCard::toThumbnailModel),
            )
        }

    val otherCards = sortedCards.filter { card ->
        card.analysisResult.contentTypes.primaryContentType == ScreenshotContentType.OTHER
    }
    val otherItems = when (othersSort) {
        CollectionListSort.Latest -> otherCards
        CollectionListSort.Name -> otherCards.sortedBy { card -> card.analysisResult.title }
    }.map(StoredScreenshotCard::toCardItemUiModel)

    return CollectionOverviewUiModel(
        favoriteSummary = favoriteSummary,
        favoriteItems = favoriteItems,
        typeSummaries = typeSummaries,
        otherItems = otherItems,
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
    val categoryType = when (filter) {
        is CollectionDetailFilter.ByType -> filter.contentType.toRecapCategoryType()
        CollectionDetailFilter.Favorites -> null
    }
    return CollectionDetailUiModel(
        titleResId = titleResId,
        count = sortedCards.size,
        sort = sort,
        cards = sortedCards.map(StoredScreenshotCard::toCardItemUiModel),
        emptyMessageResId = emptyMessageResId,
        categoryType = categoryType,
    )
}

internal sealed interface CollectionDetailFilter {
    data class ByType(val contentType: ScreenshotContentType) : CollectionDetailFilter
    data object Favorites : CollectionDetailFilter
}

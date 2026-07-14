package com.chalkak.recap.feature.collection

import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.category.toLabelResId
import com.chalkak.recap.core.design.category.toRecapCategoryType
import com.chalkak.recap.core.design.component.card.ScreenshotCardMetadataMode
import com.chalkak.recap.core.design.component.topbar.CollectionTypeViewMode
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import timber.log.Timber

enum class CollectionListSort {
    Latest,
    Oldest,
}

data class CollectionCardItemUiModel(
    val imageId: String,
    val title: String,
    val summary: String,
    val contentTypeLabelResId: Int,
    val categoryType: RecapCategoryType,
    val createdAtMillis: Long,
    val isFavorite: Boolean,
    val thumbnailModel: Any?,
)

data class CollectionTypeSummaryUiModel(
    val contentType: ScreenshotContentType,
    val labelResId: Int,
    val categoryType: RecapCategoryType,
    val count: Int,
    val exampleTitles: List<String>,
    val additionalExampleCount: Int,
)

data class CollectionFavoriteSummaryUiModel(
    val count: Int,
)

data class CollectionOverviewUiModel(
    val favoriteSummary: CollectionFavoriteSummaryUiModel = CollectionFavoriteSummaryUiModel(count = 0),
    val typeSummaries: List<CollectionTypeSummaryUiModel> = emptyList(),
)

data class CollectionDetailUiModel(
    val titleResId: Int,
    val count: Int,
    val sort: CollectionListSort,
    val cards: List<CollectionCardItemUiModel>,
    val emptyMessageResId: Int,
    val categoryType: RecapCategoryType? = null,
    val cardMetadataMode: ScreenshotCardMetadataMode = ScreenshotCardMetadataMode.OrganizedDate,
)

data class CollectionSelectionUiState(
    val isActive: Boolean = false,
    val selectedImageIds: Set<String> = emptySet(),
    val isDeleting: Boolean = false,
) {
    val selectedCount: Int
        get() = selectedImageIds.size
}

data class CollectionUiState(
    val isLoading: Boolean = true,
    val hasStoredScreenshots: Boolean = false,
    val searchQuery: String = "",
    val detailSearchQuery: String = "",
    val isDetailSearchVisible: Boolean = false,
    val typeViewMode: CollectionTypeViewMode = CollectionTypeViewMode.Grid,
    val overview: CollectionOverviewUiModel = CollectionOverviewUiModel(),
    val detail: CollectionDetailUiModel? = null,
    val selection: CollectionSelectionUiState = CollectionSelectionUiState(),
)

sealed interface CollectionAction {
    data class UpdateSearchQuery(val query: String) : CollectionAction
    data object ShowDetailSearch : CollectionAction
    data object HideDetailSearch : CollectionAction
    data class UpdateDetailSearchQuery(val query: String) : CollectionAction
    data class SetTypeViewMode(val viewMode: CollectionTypeViewMode) : CollectionAction
    data object OpenFavoriteDetail : CollectionAction
    data class OpenFavoriteItem(val imageId: String) : CollectionAction
    data class OpenTypeDetail(val contentType: ScreenshotContentType) : CollectionAction
    data object CloseDetail : CollectionAction
    data class SetDetailSort(val sort: CollectionListSort) : CollectionAction
    data class ToggleFavorite(val imageId: String) : CollectionAction
    data object StartSelection : CollectionAction
    data object CancelSelection : CollectionAction
    data class ToggleItemSelection(val imageId: String) : CollectionAction
    data class ToggleAllSelection(val imageIds: Set<String>) : CollectionAction
    data object DeleteSelected : CollectionAction
}

internal val CollectionOverviewCategoryOrder: List<ScreenshotContentType> = listOf(
    ScreenshotContentType.SHOPPING_PRODUCT,
    ScreenshotContentType.PLACE_RESTAURANT,
    ScreenshotContentType.SCHEDULE_RESERVATION,
    ScreenshotContentType.INFO_KNOWLEDGE,
    ScreenshotContentType.BOOK_CONTENT,
    ScreenshotContentType.BENEFIT_EVENT,
    ScreenshotContentType.RECORD_CAPTURE,
    ScreenshotContentType.JOB_CAREER,
    ScreenshotContentType.OTHER,
)

internal fun StoredScreenshotCard.toThumbnailModel(): Any? {
    val thumbnail = imageRefs.thumbnailPath?.takeIf { it.isNotBlank() }
    return thumbnail ?: imageRefs.storedImagePath ?: imageRefs.sourceImageUri
}

internal fun StoredScreenshotCard.toCardItemUiModel(): CollectionCardItemUiModel {
    val contentType = analysisResult.contentTypes.primaryContentType
    return CollectionCardItemUiModel(
        imageId = analysisResult.imageId,
        title = analysisResult.title,
        summary = analysisResult.summary,
        contentTypeLabelResId = contentType.toLabelResId(),
        categoryType = contentType.toRecapCategoryType(),
        createdAtMillis = createdAtMillis,
        isFavorite = analysisResult.isFavorite,
        thumbnailModel = toThumbnailModel(),
    )
}

internal fun List<StoredScreenshotCard>.toOverviewUiModel(
    searchQuery: String = "",
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
    val favoriteSummary = CollectionFavoriteSummaryUiModel(count = favoriteCards.size)

    val typeSummaries = CollectionOverviewCategoryOrder.mapNotNull { contentType ->
        val typeCards = sortedCards.filter { card ->
            card.analysisResult.contentTypes.primaryContentType == contentType
        }
        if (typeCards.isEmpty()) {
            return@mapNotNull null
        }
        val categoryType = contentType.toRecapCategoryType()
        CollectionTypeSummaryUiModel(
            contentType = contentType,
            labelResId = categoryType.labelResId,
            categoryType = categoryType,
            count = typeCards.size,
            exampleTitles = typeCards.map { card -> card.analysisResult.title }.take(2),
            additionalExampleCount = if (typeCards.size >= 3) typeCards.size - 2 else 0,
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
    searchQuery: String = "",
): CollectionDetailUiModel {
    val filteredCards = when (filter) {
        is CollectionDetailFilter.ByType -> filter { card ->
            card.analysisResult.contentTypes.primaryContentType == filter.contentType
        }
        CollectionDetailFilter.Favorites -> filter { card -> card.analysisResult.isFavorite }
    }
    val normalizedQuery = searchQuery.trim()
    val queryFilteredCards = if (normalizedQuery.isEmpty()) {
        filteredCards
    } else {
        filteredCards.filter { card ->
            card.analysisResult.title.contains(normalizedQuery, ignoreCase = true) ||
                card.analysisResult.summary.contains(normalizedQuery, ignoreCase = true)
        }
    }
    val sortedCards = when (sort) {
        CollectionListSort.Latest -> queryFilteredCards.sortedByDescending { card -> card.createdAtMillis }
        CollectionListSort.Oldest -> queryFilteredCards.sortedBy { card -> card.createdAtMillis }
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
    val cardMetadataMode = when (filter) {
        CollectionDetailFilter.Favorites -> ScreenshotCardMetadataMode.CategoryChip
        is CollectionDetailFilter.ByType -> ScreenshotCardMetadataMode.OrganizedDate
    }
    sortedCards.logThumbnailSummary()
    return CollectionDetailUiModel(
        titleResId = titleResId,
        count = sortedCards.size,
        sort = sort,
        cards = sortedCards.map(StoredScreenshotCard::toCardItemUiModel),
        emptyMessageResId = emptyMessageResId,
        categoryType = categoryType,
        cardMetadataMode = cardMetadataMode,
    )
}

private fun List<StoredScreenshotCard>.logThumbnailSummary() {
    val found = count { card -> !card.imageRefs.thumbnailPath.isNullOrBlank() }
    val fallback = size - found
    Timber.d("%d개의 이미지의 썸네일을 찾음, %d개의 이미지가 fallback됨", found, fallback)
}

internal sealed interface CollectionDetailFilter {
    data class ByType(val contentType: ScreenshotContentType) : CollectionDetailFilter
    data object Favorites : CollectionDetailFilter
}

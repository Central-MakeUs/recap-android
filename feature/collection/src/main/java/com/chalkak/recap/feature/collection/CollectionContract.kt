package com.chalkak.recap.feature.collection

import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.category.toLabelResId
import com.chalkak.recap.core.design.category.toRecapCategoryType
import com.chalkak.recap.core.design.component.card.ScreenshotCardMetadataMode
import com.chalkak.recap.core.design.component.topbar.CollectionTypeViewMode
import com.chalkak.recap.core.model.capture.CaptureList
import com.chalkak.recap.core.model.capture.CaptureSummary
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.storage.StorageOverview
import java.time.Instant
import timber.log.Timber

enum class CollectionListSort {
    Latest,
    Oldest,
}

data class CollectionCardItemUiModel(
    val captureId: Long,
    val title: String,
    val summary: String,
    val contentTypeLabelResId: Int,
    val categoryType: RecapCategoryType,
    val organizedAtMillis: Long,
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
    val selectedCaptureIds: Set<Long> = emptySet(),
    val isDeleting: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
) {
    val selectedCount: Int
        get() = selectedCaptureIds.size
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
    data class OpenFavoriteItem(val captureId: Long) : CollectionAction
    data class OpenTypeDetail(val contentType: ScreenshotContentType) : CollectionAction
    data object CloseDetail : CollectionAction
    data class SetDetailSort(val sort: CollectionListSort) : CollectionAction
    data class ToggleFavorite(val captureId: Long) : CollectionAction
    data object StartSelection : CollectionAction
    data object CancelSelection : CollectionAction
    data class ToggleItemSelection(val captureId: Long) : CollectionAction
    data object DeleteSelected : CollectionAction
    data object ConfirmDeleteSelected : CollectionAction
    data object DismissDeleteConfirmDialog : CollectionAction
}

sealed interface CollectionEvent {
    data class ShowDeleteSuccessToast(val deletedCount: Int) : CollectionEvent
    data class ShowDeletePartialFailureToast(
        val deletedCount: Int,
        val failedCount: Int,
    ) : CollectionEvent

    data object ShowDeleteFailureToast : CollectionEvent
}

internal fun StorageOverview.toOverviewUiModel(): CollectionOverviewUiModel {
    return CollectionOverviewUiModel(
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
}

internal fun CaptureSummary.toCardItemUiModel(): CollectionCardItemUiModel {
    val contentType = typeCode
    return CollectionCardItemUiModel(
        captureId = captureId,
        title = title,
        summary = summary,
        contentTypeLabelResId = contentType.toLabelResId(),
        categoryType = contentType.toRecapCategoryType(),
        organizedAtMillis = organizedAtMillis(),
        isFavorite = isFavorite,
        thumbnailModel = thumbnailModel(),
    )
}

internal fun CaptureList.toDetailUiModel(
    filter: CollectionDetailFilter,
    sort: CollectionListSort,
): CollectionDetailUiModel {
    items.logThumbnailSummary()
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
    return CollectionDetailUiModel(
        titleResId = titleResId,
        count = count,
        sort = sort,
        cards = items.map(CaptureSummary::toCardItemUiModel),
        emptyMessageResId = emptyMessageResId,
        categoryType = categoryType,
        cardMetadataMode = cardMetadataMode,
    )
}

internal fun CaptureSummary.thumbnailModel(): Any? = thumbnailUrl?.takeIf { it.isNotBlank() }

internal fun CaptureSummary.organizedAtMillis(): Long {
    return runCatching { Instant.parse(organizedAt).toEpochMilli() }.getOrDefault(0L)
}

private fun List<CaptureSummary>.logThumbnailSummary() {
    val found = count { summary -> !summary.thumbnailUrl.isNullOrBlank() }
    val fallback = size - found
    Timber.d("%d개의 이미지의 썸네일을 찾음, %d개의 이미지가 fallback됨", found, fallback)
}

internal sealed interface CollectionDetailFilter {
    data class ByType(val contentType: ScreenshotContentType) : CollectionDetailFilter
    data object Favorites : CollectionDetailFilter
}

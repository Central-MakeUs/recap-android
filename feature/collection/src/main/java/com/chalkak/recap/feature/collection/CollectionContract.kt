package com.chalkak.recap.feature.collection

import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.component.card.ScreenshotCardMetadataMode
import com.chalkak.recap.core.design.component.topbar.CollectionTypeViewMode
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType

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
    val titleHighlightRange: IntRange? = null,
    val descriptionHighlightRange: IntRange? = null,
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
    val hasNext: Boolean = false,
    val isLoadingMore: Boolean = false,
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
    val isLoadError: Boolean = false,
    val hasStoredScreenshots: Boolean = false,
    val searchQuery: String = "",
    val detailSearchQuery: String = "",
    val isDetailSearchVisible: Boolean = false,
    val typeViewMode: CollectionTypeViewMode = CollectionTypeViewMode.Grid,
    val overview: CollectionOverviewUiModel = CollectionOverviewUiModel(),
    val detail: CollectionDetailUiModel? = null,
    val selection: CollectionSelectionUiState = CollectionSelectionUiState(),
    val pendingDeleteCaptureId: Long? = null,
)

sealed interface CollectionAction {
    data class UpdateSearchQuery(val query: String) : CollectionAction
    data object OpenSearch : CollectionAction
    data object RetryLoad : CollectionAction
    data object ShowDetailSearch : CollectionAction
    data object HideDetailSearch : CollectionAction
    data class UpdateDetailSearchQuery(val query: String) : CollectionAction
    data object SubmitDetailSearch : CollectionAction
    data object LoadMoreDetailSearch : CollectionAction
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
    data class RequestDeleteItem(val captureId: Long) : CollectionAction
    data object ConfirmDeleteItem : CollectionAction
    data object DismissDeleteItem : CollectionAction
}

sealed interface CollectionEvent {
    data class ShowFavoriteToast(val isFavorite: Boolean) : CollectionEvent
    data class ShowDeleteSuccessToast(val deletedCount: Int) : CollectionEvent
    data class ShowDeletePartialFailureToast(
        val deletedCount: Int,
        val failedCount: Int,
    ) : CollectionEvent

    data object ShowDeleteFailureToast : CollectionEvent
}

internal sealed interface CollectionDetailFilter {
    data class ByType(val contentType: ScreenshotContentType) : CollectionDetailFilter
    data object Favorites : CollectionDetailFilter
}

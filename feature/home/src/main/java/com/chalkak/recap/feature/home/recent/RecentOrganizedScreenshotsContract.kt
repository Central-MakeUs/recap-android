package com.chalkak.recap.feature.home.recent

import com.chalkak.recap.core.design.category.RecapCategoryType

enum class RecentOrganizedScreenshotsPhase {
    Loading,
    Content,
    Empty,
    Error,
}

data class RecentOrganizedScreenshotsUiState(
    val phase: RecentOrganizedScreenshotsPhase = RecentOrganizedScreenshotsPhase.Loading,
    val items: List<RecentOrganizedScreenshotUiModel> = emptyList(),
    val resultCount: Long = 0L,
    val hasNext: Boolean = false,
    val nextPage: Int = 0,
    val isLoadingMore: Boolean = false,
)

data class RecentOrganizedScreenshotUiModel(
    val id: Long,
    val thumbnailModel: Any?,
    val categoryType: RecapCategoryType,
    val title: String,
    val description: String,
    val organizedAtMillis: Long,
    val isFavorite: Boolean,
)

sealed interface RecentOrganizedScreenshotsAction {
    data object NavigateBack : RecentOrganizedScreenshotsAction
    data object OpenSearch : RecentOrganizedScreenshotsAction
    data object StartImport : RecentOrganizedScreenshotsAction
    data object LoadMore : RecentOrganizedScreenshotsAction
    data object Retry : RecentOrganizedScreenshotsAction
    data class SelectItem(val id: Long) : RecentOrganizedScreenshotsAction
    data class ToggleFavorite(val id: Long) : RecentOrganizedScreenshotsAction
}

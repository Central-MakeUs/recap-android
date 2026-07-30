package com.chalkak.recap.feature.home

import com.chalkak.recap.core.design.category.RecapCategoryType

data class HomeUiState(
    val phase: HomeContentPhase = HomeContentPhase.Content,
    val recentScreenshots: List<HomeRecentScreenshotUiModel> = emptyList(),
    val favoriteItems: List<HomeFavoriteItemUiModel> = emptyList(),
    val frequentSaveTypes: List<HomeFrequentSaveTypeUiModel> = emptyList(),
)

enum class HomeContentPhase {
    Content,
    Error,
}

data class HomeRecentScreenshotUiModel(
    val id: Long,
    val thumbnailModel: Any?,
    val title: String,
    val categoryType: RecapCategoryType,
)

data class HomeFavoriteItemUiModel(
    val id: Long,
    val thumbnailModel: Any?,
    val categoryType: RecapCategoryType,
    val title: String,
    val description: String,
    val organizedAtMillis: Long,
    val isFavorite: Boolean,
)

data class HomeFrequentSaveTypeUiModel(
    val id: String,
    val categoryType: RecapCategoryType,
    val recapCount: Int,
)

sealed interface HomeAction {
    data object StartImport : HomeAction
    data object EnterDeveloperOptions : HomeAction
    data object OpenSettings : HomeAction
    data object OpenSearch : HomeAction
    data object OpenRecentScreenshots : HomeAction
    data class SelectRecentScreenshot(val id: Long) : HomeAction
    data object OpenFavoriteCategories : HomeAction
    data class SelectFavoriteItem(val id: Long) : HomeAction
    data class ToggleFavoriteItem(val id: Long) : HomeAction
    data object OpenFrequentSaveTypes : HomeAction
    data class SelectFrequentSaveType(val contentTypeName: String) : HomeAction
    data object RetryLoad : HomeAction
}

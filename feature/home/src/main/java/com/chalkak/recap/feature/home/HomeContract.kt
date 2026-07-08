package com.chalkak.recap.feature.home

import androidx.annotation.StringRes
import androidx.annotation.DrawableRes
import com.chalkak.recap.core.design.R

data class HomeAnalysisProgressUiModel(
    val isRunning: Boolean = false,
    val progress: Float = 0f,
)

data class HomeUiState(
    val recentScreenshots: List<HomeRecentScreenshotUiModel> = HomeMockRecentScreenshots,
    val favoriteCategories: List<HomeFavoriteCategoryUiModel> = HomeMockFavoriteCategories,
    val frequentSaveTypes: List<HomeFrequentSaveTypeUiModel> = HomeMockFrequentSaveTypes,
)

data class HomeRecentScreenshotUiModel(
    val id: String,
    @get:DrawableRes val thumbnailResId: Int,
    @get:StringRes val titleResId: Int,
    @get:StringRes val categoryLabelResId: Int,
)

data class HomeFavoriteCategoryUiModel(
    val id: String,
    @get:DrawableRes val thumbnailResId: Int,
    @get:StringRes val categoryLabelResId: Int,
    @get:StringRes val titleResId: Int,
    @get:StringRes val descriptionResId: Int,
    val isFavorite: Boolean,
)

data class HomeFrequentSaveTypeUiModel(
    val id: String,
    @get:StringRes val categoryLabelResId: Int,
    val recapCount: Int,
)

sealed interface HomeAction {
    data object StartImport : HomeAction
    data object EnterDeveloperOptions : HomeAction
    data object OpenRecentScreenshots : HomeAction
    data class SelectRecentScreenshot(val id: String) : HomeAction
    data object OpenFavoriteCategories : HomeAction
    data class SelectFavoriteCategory(val id: String) : HomeAction
    data class ToggleFavoriteCategory(val id: String) : HomeAction
    data object OpenFrequentSaveTypes : HomeAction
    data class SelectFrequentSaveType(val id: String) : HomeAction
}

private val HomeMockRecentScreenshots = listOf(
    HomeRecentScreenshotUiModel(
        id = "return",
        thumbnailResId = R.drawable.mock_home_screenshot_return,
        titleResId = R.string.home_recent_screenshot_return_title,
        categoryLabelResId = R.string.home_category_shopping_product,
    ),
    HomeRecentScreenshotUiModel(
        id = "hotel",
        thumbnailResId = R.drawable.mock_home_screenshot_hotel,
        titleResId = R.string.home_recent_screenshot_hotel_title,
        categoryLabelResId = R.string.home_category_schedule_reservation,
    ),
    HomeRecentScreenshotUiModel(
        id = "recipe",
        thumbnailResId = R.drawable.mock_home_screenshot_recipe,
        titleResId = R.string.home_recent_screenshot_recipe_title,
        categoryLabelResId = R.string.home_category_info_knowledge,
    ),
)

private val HomeMockFavoriteCategories = listOf(
    HomeFavoriteCategoryUiModel(
        id = "tax",
        thumbnailResId = R.drawable.mock_home_screenshot_tax,
        categoryLabelResId = R.string.home_category_category_01,
        titleResId = R.string.home_favorite_year_end_tax_title,
        descriptionResId = R.string.home_favorite_year_end_tax_description,
        isFavorite = true,
    ),
    HomeFavoriteCategoryUiModel(
        id = "moving",
        thumbnailResId = R.drawable.mock_home_screenshot_hotel,
        categoryLabelResId = R.string.home_category_category_01,
        titleResId = R.string.home_favorite_moving_checklist_title,
        descriptionResId = R.string.home_favorite_moving_checklist_description,
        isFavorite = true,
    ),
    HomeFavoriteCategoryUiModel(
        id = "keyboard",
        thumbnailResId = R.drawable.mock_home_screenshot_return,
        categoryLabelResId = R.string.home_category_shopping_product,
        titleResId = R.string.home_favorite_keyboard_title,
        descriptionResId = R.string.home_favorite_keyboard_description,
        isFavorite = true,
    ),
)

private val HomeMockFrequentSaveTypes = listOf(
    HomeFrequentSaveTypeUiModel(
        id = "shopping-product",
        categoryLabelResId = R.string.home_category_shopping_product,
        recapCount = 12,
    ),
    HomeFrequentSaveTypeUiModel(
        id = "place-restaurant",
        categoryLabelResId = R.string.home_category_place_restaurant,
        recapCount = 8,
    ),
    HomeFrequentSaveTypeUiModel(
        id = "schedule-reservation",
        categoryLabelResId = R.string.home_category_schedule_reservation,
        recapCount = 5,
    ),
    HomeFrequentSaveTypeUiModel(
        id = "shopping-product-alt",
        categoryLabelResId = R.string.home_category_shopping_product,
        recapCount = 3,
    ),
)

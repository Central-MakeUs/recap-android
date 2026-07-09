package com.chalkak.recap.feature.home

import androidx.annotation.StringRes
import androidx.annotation.DrawableRes
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.RecapCategoryType
import java.util.concurrent.TimeUnit

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
    val categoryType: RecapCategoryType,
)

data class HomeFavoriteCategoryUiModel(
    val id: String,
    @get:DrawableRes val thumbnailResId: Int,
    val categoryType: RecapCategoryType,
    @get:StringRes val titleResId: Int,
    @get:StringRes val descriptionResId: Int,
    val organizedAtMillis: Long,
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
    data object OpenSettings : HomeAction
    data object OpenSearch : HomeAction
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
        categoryType = RecapCategoryType.ShoppingProduct,
    ),
    HomeRecentScreenshotUiModel(
        id = "hotel",
        thumbnailResId = R.drawable.mock_home_screenshot_hotel,
        titleResId = R.string.home_recent_screenshot_hotel_title,
        categoryType = RecapCategoryType.ScheduleReservation,
    ),
    HomeRecentScreenshotUiModel(
        id = "recipe",
        thumbnailResId = R.drawable.mock_home_screenshot_recipe,
        titleResId = R.string.home_recent_screenshot_recipe_title,
        categoryType = RecapCategoryType.InfoKnowledge,
    ),
)

private val HomeMockFavoriteCategories = listOf(
    HomeFavoriteCategoryUiModel(
        id = "tax",
        thumbnailResId = R.drawable.mock_home_screenshot_tax,
        categoryType = RecapCategoryType.InfoKnowledge,
        titleResId = R.string.home_favorite_year_end_tax_title,
        descriptionResId = R.string.home_favorite_year_end_tax_description,
        organizedAtMillis = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1),
        isFavorite = true,
    ),
    HomeFavoriteCategoryUiModel(
        id = "moving",
        thumbnailResId = R.drawable.mock_home_screenshot_hotel,
        categoryType = RecapCategoryType.ScheduleReservation,
        titleResId = R.string.home_favorite_moving_checklist_title,
        descriptionResId = R.string.home_favorite_moving_checklist_description,
        organizedAtMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
        isFavorite = true,
    ),
    HomeFavoriteCategoryUiModel(
        id = "keyboard",
        thumbnailResId = R.drawable.mock_home_screenshot_return,
        categoryType = RecapCategoryType.ShoppingProduct,
        titleResId = R.string.home_favorite_keyboard_title,
        descriptionResId = R.string.home_favorite_keyboard_description,
        organizedAtMillis = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30),
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

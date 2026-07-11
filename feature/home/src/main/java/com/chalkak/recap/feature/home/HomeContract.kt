package com.chalkak.recap.feature.home

import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.RecapCategoryType
import java.util.concurrent.TimeUnit

data class HomeAnalysisProgressUiModel(
    val isRunning: Boolean = false,
    val progress: Float = 0f,
)

data class HomeUiState(
    val recentScreenshots: List<HomeRecentScreenshotUiModel> = emptyList(),
    val favoriteItems: List<HomeFavoriteItemUiModel> = emptyList(),
    val frequentSaveTypes: List<HomeFrequentSaveTypeUiModel> = emptyList(),
)

data class HomeRecentScreenshotUiModel(
    val id: String,
    val thumbnailModel: Any?,
    val title: String,
    val categoryType: RecapCategoryType,
)

data class HomeFavoriteItemUiModel(
    val id: String,
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
    data class SelectRecentScreenshot(val id: String) : HomeAction
    data object OpenFavoriteCategories : HomeAction
    data class SelectFavoriteItem(val id: String) : HomeAction
    data class ToggleFavoriteItem(val id: String) : HomeAction
    data object OpenFrequentSaveTypes : HomeAction
    data class SelectFrequentSaveType(val id: String) : HomeAction
}

internal val HomePreviewUiState = HomeUiState(
    recentScreenshots = listOf(
        HomeRecentScreenshotUiModel(
            id = "return",
            thumbnailModel = R.drawable.mock_home_screenshot_return,
            title = "택배 반품 절차 정리",
            categoryType = RecapCategoryType.ShoppingProduct,
        ),
        HomeRecentScreenshotUiModel(
            id = "hotel",
            thumbnailModel = R.drawable.mock_home_screenshot_hotel,
            title = "제주 숙소 예약 정보",
            categoryType = RecapCategoryType.ScheduleReservation,
        ),
        HomeRecentScreenshotUiModel(
            id = "recipe",
            thumbnailModel = R.drawable.mock_home_screenshot_recipe,
            title = "파스타 레시피 저장",
            categoryType = RecapCategoryType.InfoKnowledge,
        ),
    ),
    favoriteItems = listOf(
        HomeFavoriteItemUiModel(
            id = "tax",
            thumbnailModel = R.drawable.mock_home_screenshot_tax,
            categoryType = RecapCategoryType.RecordCapture,
            title = "연말정산 서류 목록",
            description = "연말정산 제출에 필요한 서류 정리",
            organizedAtMillis = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1),
            isFavorite = true,
        ),
        HomeFavoriteItemUiModel(
            id = "moving",
            thumbnailModel = R.drawable.mock_home_screenshot_hotel,
            categoryType = RecapCategoryType.ScheduleReservation,
            title = "이사 체크리스트",
            description = "이사 준비시 꼭 해야할 체크리스트",
            organizedAtMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
            isFavorite = true,
        ),
        HomeFavoriteItemUiModel(
            id = "keyboard",
            thumbnailModel = R.drawable.mock_home_screenshot_return,
            categoryType = RecapCategoryType.ShoppingProduct,
            title = "무선 키보드 후보",
            description = "가격과 배송 정보가 포함된 상품 캡처",
            organizedAtMillis = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30),
            isFavorite = true,
        ),
    ),
    frequentSaveTypes = listOf(
        HomeFrequentSaveTypeUiModel(
            id = RecapCategoryType.ShoppingProduct.name,
            categoryType = RecapCategoryType.ShoppingProduct,
            recapCount = 12,
        ),
        HomeFrequentSaveTypeUiModel(
            id = RecapCategoryType.PlaceRestaurant.name,
            categoryType = RecapCategoryType.PlaceRestaurant,
            recapCount = 8,
        ),
        HomeFrequentSaveTypeUiModel(
            id = RecapCategoryType.BookContent.name,
            categoryType = RecapCategoryType.BookContent,
            recapCount = 5,
        ),
    ),
)

package com.chalkak.recap.feature.home

import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.RecapCategoryType
import java.util.concurrent.TimeUnit

data class RecentOrganizedScreenshotsUiState(
    val items: List<RecentOrganizedScreenshotUiModel> = emptyList(),
)

data class RecentOrganizedScreenshotUiModel(
    val id: String,
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
    data class SelectItem(val id: String) : RecentOrganizedScreenshotsAction
    data class ToggleFavorite(val id: String) : RecentOrganizedScreenshotsAction
}

internal val RecentOrganizedScreenshotsPreviewUiState = RecentOrganizedScreenshotsUiState(
    items = listOf(
        RecentOrganizedScreenshotUiModel(
            id = "return",
            thumbnailModel = R.drawable.mock_home_screenshot_return,
            categoryType = RecapCategoryType.RecordCapture,
            title = "택배 반품 절차 정리",
            description = "한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약",
            organizedAtMillis = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1),
            isFavorite = false,
        ),
        RecentOrganizedScreenshotUiModel(
            id = "hotel",
            thumbnailModel = R.drawable.mock_home_screenshot_hotel,
            categoryType = RecapCategoryType.ScheduleReservation,
            title = "제주 숙소 예약 정보",
            description = "한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약",
            organizedAtMillis = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2),
            isFavorite = false,
        ),
        RecentOrganizedScreenshotUiModel(
            id = "recipe",
            thumbnailModel = R.drawable.mock_home_screenshot_recipe,
            categoryType = RecapCategoryType.InfoKnowledge,
            title = "파스타 레시피 저장",
            description = "한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약",
            organizedAtMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
            isFavorite = false,
        ),
    ),
)

package com.chalkak.recap.feature.home.recent

import com.chalkak.recap.core.design.category.toRecapCategoryType
import com.chalkak.recap.core.model.capture.CaptureSummary
import com.chalkak.recap.feature.home.organizedAtMillis
import com.chalkak.recap.feature.home.thumbnailModel

internal fun List<CaptureSummary>.toRecentOrganizedScreenshotsUiState(): RecentOrganizedScreenshotsUiState {
    val items = map { summary ->
        RecentOrganizedScreenshotUiModel(
            id = summary.captureId,
            thumbnailModel = summary.thumbnailModel(),
            categoryType = summary.typeCode.toRecapCategoryType(),
            title = summary.title,
            description = summary.summary,
            organizedAtMillis = summary.organizedAtMillis(),
            isFavorite = summary.isFavorite,
        )
    }
    return RecentOrganizedScreenshotsUiState(items = items)
}

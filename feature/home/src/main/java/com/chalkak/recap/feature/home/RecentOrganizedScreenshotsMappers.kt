package com.chalkak.recap.feature.home

import com.chalkak.recap.core.design.category.toRecapCategoryType
import com.chalkak.recap.core.model.capture.CaptureSummary

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

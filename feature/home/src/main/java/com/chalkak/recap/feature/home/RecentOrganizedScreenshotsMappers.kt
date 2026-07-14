package com.chalkak.recap.feature.home

import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.design.category.toRecapCategoryType

internal fun List<StoredScreenshotCard>.toRecentOrganizedScreenshotsUiState(): RecentOrganizedScreenshotsUiState {
    val items = sortedByDescending { card -> card.createdAtMillis }
        .map { card ->
            RecentOrganizedScreenshotUiModel(
                id = card.analysisResult.imageId,
                thumbnailModel = card.toThumbnailModel(),
                categoryType = card.analysisResult.contentTypes.primaryContentType.toRecapCategoryType(),
                title = card.analysisResult.title,
                description = card.analysisResult.summary,
                organizedAtMillis = card.createdAtMillis,
                isFavorite = card.analysisResult.isFavorite,
            )
        }
    return RecentOrganizedScreenshotsUiState(items = items)
}

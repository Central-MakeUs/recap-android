package com.chalkak.recap.feature.home

import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.design.category.toRecapCategoryType

internal fun List<StoredScreenshotCard>.toRecentOrganizedScreenshotsUiState(): RecentOrganizedScreenshotsUiState {
    val items = sortedByDescending { card -> card.analysisResult.organizedAt.toEpochMilli() }
        .map { card ->
            RecentOrganizedScreenshotUiModel(
                id = card.analysisResult.captureId,
                thumbnailModel = card.toThumbnailModel(),
                categoryType = card.analysisResult.typeCode.toRecapCategoryType(),
                title = card.analysisResult.title,
                description = card.analysisResult.summary,
                organizedAtMillis = card.analysisResult.organizedAt.toEpochMilli(),
                isFavorite = card.analysisResult.isFavorite,
            )
        }
    return RecentOrganizedScreenshotsUiState(items = items)
}

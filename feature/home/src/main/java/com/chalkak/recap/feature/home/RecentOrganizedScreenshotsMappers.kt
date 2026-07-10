package com.chalkak.recap.feature.home

import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard

internal fun List<StoredScreenshotCard>.toRecentOrganizedScreenshotsUiState(): RecentOrganizedScreenshotsUiState {
    val items = sortedByDescending { card -> card.createdAtMillis }
        .mapNotNull { card ->
            val categoryType = card.analysisResult.contentTypes.primaryContentType
                .toRecapCategoryType()
                ?: return@mapNotNull null
            RecentOrganizedScreenshotUiModel(
                id = card.analysisResult.imageId,
                thumbnailModel = card.toThumbnailModel(),
                categoryType = categoryType,
                title = card.analysisResult.title,
                description = card.analysisResult.summary,
                organizedAtMillis = card.createdAtMillis,
                isFavorite = card.analysisResult.isFavorite,
            )
        }
    return RecentOrganizedScreenshotsUiState(items = items)
}

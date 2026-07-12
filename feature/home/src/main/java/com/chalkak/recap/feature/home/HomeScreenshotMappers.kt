package com.chalkak.recap.feature.home

import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.design.category.toRecapCategoryType
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType

private const val HomeRecentScreenshotLimit = 3
private const val HomeFavoriteItemLimit = 3
private const val HomeFrequentSaveTypeLimit = 3

internal fun StoredScreenshotCard.toThumbnailModel(): Any? {
    return imageRefs.thumbnailPath ?: imageRefs.storedImagePath ?: imageRefs.sourceImageUri
}

internal fun List<StoredScreenshotCard>.toHomeUiState(): HomeUiState {
    val sortedCards = sortedByDescending { card -> card.createdAtMillis }

    val recentScreenshots = sortedCards.mapNotNull { card ->
        val categoryType = card.analysisResult.contentTypes.primaryContentType.toRecapCategoryType()
            ?: return@mapNotNull null
        HomeRecentScreenshotUiModel(
            id = card.analysisResult.imageId,
            thumbnailModel = card.toThumbnailModel(),
            title = card.analysisResult.title,
            categoryType = categoryType,
        )
    }.take(HomeRecentScreenshotLimit)

    val favoriteItems = sortedCards
        .filter { card -> card.analysisResult.isFavorite }
        .mapNotNull { card ->
            val categoryType = card.analysisResult.contentTypes.primaryContentType.toRecapCategoryType()
                ?: return@mapNotNull null
            HomeFavoriteItemUiModel(
                id = card.analysisResult.imageId,
                thumbnailModel = card.toThumbnailModel(),
                categoryType = categoryType,
                title = card.analysisResult.title,
                description = card.analysisResult.summary,
                organizedAtMillis = card.createdAtMillis,
                isFavorite = card.analysisResult.isFavorite,
            )
        }
        .take(HomeFavoriteItemLimit)

    val frequentSaveTypes = ScreenshotContentType.entries
        .mapNotNull { contentType ->
            val categoryType = contentType.toRecapCategoryType() ?: return@mapNotNull null
            val count = sortedCards.count { card ->
                card.analysisResult.contentTypes.primaryContentType == contentType
            }
            if (count <= 0) {
                return@mapNotNull null
            }
            HomeFrequentSaveTypeUiModel(
                id = categoryType.name,
                categoryType = categoryType,
                recapCount = count,
            )
        }
        .sortedByDescending { saveType -> saveType.recapCount }
        .take(HomeFrequentSaveTypeLimit)

    return HomeUiState(
        recentScreenshots = recentScreenshots,
        favoriteItems = favoriteItems,
        frequentSaveTypes = frequentSaveTypes,
    )
}

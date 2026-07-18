package com.chalkak.recap.feature.home

import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.design.category.toRecapCategoryType
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import timber.log.Timber

private const val HomeRecentScreenshotLimit = 3
private const val HomeFavoriteItemLimit = 4
private const val HomeFrequentSaveTypeLimit = 4

internal fun StoredScreenshotCard.toThumbnailModel(): Any? {
    val thumbnail = imageRefs.thumbnailPath?.takeIf { it.isNotBlank() }
    return thumbnail ?: imageRefs.storedImagePath ?: imageRefs.sourceImageUri
}

internal fun List<StoredScreenshotCard>.toHomeUiState(): HomeUiState {
    val sortedCards = sortedByDescending { card -> card.analysisResult.organizedAt.toEpochMilli() }

    val recentSource = sortedCards.take(HomeRecentScreenshotLimit)
    val recentScreenshots = recentSource.map { card ->
        HomeRecentScreenshotUiModel(
            id = card.analysisResult.captureId,
            thumbnailModel = card.toThumbnailModel(),
            title = card.analysisResult.title,
            categoryType = card.analysisResult.typeCode.toRecapCategoryType(),
        )
    }

    val favoriteSource = sortedCards
        .filter { card -> card.analysisResult.isFavorite }
        .take(HomeFavoriteItemLimit)
    val favoriteItems = favoriteSource.map { card ->
        HomeFavoriteItemUiModel(
            id = card.analysisResult.captureId,
            thumbnailModel = card.toThumbnailModel(),
            categoryType = card.analysisResult.typeCode.toRecapCategoryType(),
            title = card.analysisResult.title,
            description = card.analysisResult.summary,
            organizedAtMillis = card.analysisResult.organizedAt.toEpochMilli(),
            isFavorite = card.analysisResult.isFavorite,
        )
    }

    (recentSource + favoriteSource)
        .distinctBy { card -> card.analysisResult.captureId }
        .logThumbnailSummary()

    val frequentSaveTypes = ScreenshotContentType.entries
        .mapNotNull { contentType ->
            val count = sortedCards.count { card ->
                card.analysisResult.typeCode == contentType
            }
            if (count <= 0) {
                return@mapNotNull null
            }
            val categoryType = contentType.toRecapCategoryType()
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

private fun List<StoredScreenshotCard>.logThumbnailSummary() {
    val found = count { card -> !card.imageRefs.thumbnailPath.isNullOrBlank() }
    val fallback = size - found
    Timber.d("%d개의 이미지의 썸네일을 찾음, %d개의 이미지가 fallback됨", found, fallback)
}

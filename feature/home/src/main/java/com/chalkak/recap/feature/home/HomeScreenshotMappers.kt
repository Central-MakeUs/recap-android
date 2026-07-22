package com.chalkak.recap.feature.home

import com.chalkak.recap.core.design.category.toRecapCategoryType
import com.chalkak.recap.core.model.capture.CaptureSummary
import com.chalkak.recap.core.model.home.HomeSummary
import java.time.Instant
import timber.log.Timber

internal fun HomeSummary.toHomeUiState(): HomeUiState {
    (recentCaptures + favorites)
        .distinctBy { summary -> summary.captureId }
        .logThumbnailSummary()

    return HomeUiState(
        recentScreenshots = recentCaptures.map { summary ->
            HomeRecentScreenshotUiModel(
                id = summary.captureId,
                thumbnailModel = summary.thumbnailModel(),
                title = summary.title,
                categoryType = summary.typeCode.toRecapCategoryType(),
            )
        },
        favoriteItems = favorites.map { summary ->
            HomeFavoriteItemUiModel(
                id = summary.captureId,
                thumbnailModel = summary.thumbnailModel(),
                categoryType = summary.typeCode.toRecapCategoryType(),
                title = summary.title,
                description = summary.summary,
                organizedAtMillis = summary.organizedAtMillis(),
                isFavorite = summary.isFavorite,
            )
        },
        frequentSaveTypes = topTypes.map { topType ->
            val categoryType = topType.typeCode.toRecapCategoryType()
            HomeFrequentSaveTypeUiModel(
                id = categoryType.name,
                categoryType = categoryType,
                recapCount = topType.count.toInt(),
            )
        },
    )
}

internal fun CaptureSummary.thumbnailModel(): Any? = thumbnailUrl?.takeIf { it.isNotBlank() }

internal fun CaptureSummary.organizedAtMillis(): Long {
    return runCatching { Instant.parse(organizedAt).toEpochMilli() }.getOrDefault(0L)
}

private fun List<CaptureSummary>.logThumbnailSummary() {
    val found = count { summary -> !summary.thumbnailUrl.isNullOrBlank() }
    val fallback = size - found
    Timber.d("%d개의 이미지의 썸네일을 찾음, %d개의 이미지가 fallback됨", found, fallback)
}

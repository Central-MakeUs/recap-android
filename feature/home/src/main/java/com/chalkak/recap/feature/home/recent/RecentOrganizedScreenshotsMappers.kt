package com.chalkak.recap.feature.home.recent

import com.chalkak.recap.core.design.category.toRecapCategoryType
import com.chalkak.recap.core.model.capture.CapturePage
import com.chalkak.recap.core.model.capture.CaptureSummary
import com.chalkak.recap.feature.home.organizedAtMillis
import com.chalkak.recap.feature.home.thumbnailModel
import timber.log.Timber

internal fun CaptureSummary.toRecentOrganizedScreenshotUiModel(): RecentOrganizedScreenshotUiModel =
    RecentOrganizedScreenshotUiModel(
        id = captureId,
        thumbnailModel = thumbnailModel(),
        categoryType = typeCode.toRecapCategoryType(),
        title = title,
        description = summary,
        organizedAtMillis = organizedAtMillis(),
        isFavorite = isFavorite,
    )

internal fun CapturePage.toRecentOrganizedScreenshotItems(): List<RecentOrganizedScreenshotUiModel> {
    items.logThumbnailSummary()
    return items.map { summary -> summary.toRecentOrganizedScreenshotUiModel() }
}

private fun List<CaptureSummary>.logThumbnailSummary() {
    val found = count { summary -> !summary.thumbnailUrl.isNullOrBlank() }
    val fallback = size - found
    Timber.d("%d개의 이미지의 썸네일을 찾음, %d개의 이미지가 fallback됨", found, fallback)
}

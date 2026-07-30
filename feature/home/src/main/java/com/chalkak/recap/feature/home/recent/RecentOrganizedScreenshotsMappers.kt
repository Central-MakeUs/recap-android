package com.chalkak.recap.feature.home.recent

import com.chalkak.recap.core.design.category.toRecapCategoryType
import com.chalkak.recap.core.model.capture.CapturePage
import com.chalkak.recap.core.model.capture.CaptureSummary
import com.chalkak.recap.feature.home.organizedAtMillis
import com.chalkak.recap.feature.home.thumbnailModel

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

internal fun CapturePage.toRecentOrganizedScreenshotItems(): List<RecentOrganizedScreenshotUiModel> =
    items.map { summary -> summary.toRecentOrganizedScreenshotUiModel() }

package com.chalkak.recap.core.model.home

import com.chalkak.recap.core.model.capture.CaptureSummary
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType

data class HomeSummary(
    val recentCaptures: List<CaptureSummary>,
    val favorites: List<CaptureSummary>,
    val topTypes: List<TopType>,
    val hasAnyCapture: Boolean,
)

data class TopType(
    val typeCode: ScreenshotContentType,
    val count: Long,
    val representativeThumbnailUrl: String?,
)

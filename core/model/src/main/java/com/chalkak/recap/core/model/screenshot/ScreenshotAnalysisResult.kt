package com.chalkak.recap.core.model.screenshot

import java.time.Instant

data class ScreenshotAnalysisResult(
    val captureId: Long,
    val typeCode: ScreenshotContentType,
    val title: String,
    val summary: String,
    val body: String,
    val originalImageUrl: String,
    val isFavorite: Boolean,
    val organizedAt: Instant,
)

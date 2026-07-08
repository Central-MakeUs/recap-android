package com.chalkak.recap.core.model.screenshot

data class ScreenshotAnalysisResult(
    val imageId: String,
    val title: String,
    val summary: String,
    val contentTypes: ScreenshotContentTypes,
    val keyFields: List<ScreenshotKeyField>,
    val confidence: ScreenshotAnalysisConfidence,
)

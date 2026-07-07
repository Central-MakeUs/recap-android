package com.chalkak.recap.core.model

data class ScreenshotAsset(
    val id: String,
    val uri: String,
    val capturedAtMillis: Long,
    val importedAtMillis: Long,
)

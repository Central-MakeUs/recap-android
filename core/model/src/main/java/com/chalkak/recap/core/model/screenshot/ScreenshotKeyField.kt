package com.chalkak.recap.core.model.screenshot

data class ScreenshotKeyField(
    val label: String,
    val value: String,
    val displayPriority: Int,
    val isSensitive: Boolean,
)

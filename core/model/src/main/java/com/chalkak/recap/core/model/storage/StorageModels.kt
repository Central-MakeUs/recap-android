package com.chalkak.recap.core.model.storage

import com.chalkak.recap.core.model.screenshot.ScreenshotContentType

data class StorageType(
    val typeCode: ScreenshotContentType,
    val count: Long,
    val representativeTitles: List<String>,
)

data class StorageOverview(
    val hasAnyCapture: Boolean,
    val favoriteCount: Int,
    val types: List<StorageType>,
)

enum class CaptureSort {
    Latest,
    Oldest,
}

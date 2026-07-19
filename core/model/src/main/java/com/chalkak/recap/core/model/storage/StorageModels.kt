package com.chalkak.recap.core.model.storage

import com.chalkak.recap.core.model.screenshot.ScreenshotContentType

data class StorageType(
    val typeCode: ScreenshotContentType,
    val count: Long,
    val representativeTitles: List<String>,
)

enum class CaptureSort {
    Latest,
    Oldest,
}

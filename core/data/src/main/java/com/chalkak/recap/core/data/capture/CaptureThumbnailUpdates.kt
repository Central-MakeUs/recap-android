package com.chalkak.recap.core.data.capture

import kotlinx.coroutines.flow.SharedFlow

data class CaptureThumbnailReady(
    val captureId: Long,
    val localPath: String,
)

interface CaptureThumbnailUpdates {
    val thumbnailReady: SharedFlow<CaptureThumbnailReady>

    fun resolveLocalPath(captureId: Long): String?
}

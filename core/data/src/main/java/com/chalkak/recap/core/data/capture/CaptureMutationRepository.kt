package com.chalkak.recap.core.data.capture

import com.chalkak.recap.core.model.capture.CaptureDeleteResult
import com.chalkak.recap.core.model.capture.ReportReason
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType

interface CaptureMutationRepository {
    suspend fun updateFavorite(
        captureId: Long,
        isFavorite: Boolean,
    ): Result<Unit>

    suspend fun updateCapture(
        captureId: Long,
        title: String,
        summary: String,
        body: String,
        typeCode: ScreenshotContentType,
    ): Result<Unit>

    suspend fun deleteCaptures(captureIds: Set<Long>): Result<CaptureDeleteResult>

    suspend fun report(
        captureId: Long,
        reason: ReportReason,
        detail: String? = null,
    ): Result<Unit>
}

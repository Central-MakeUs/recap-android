package com.chalkak.recap.core.data.capture

import com.chalkak.recap.core.model.capture.CaptureDeleteResult
import com.chalkak.recap.core.model.capture.ReportReason

interface CaptureMutationRepository {
    suspend fun updateFavorite(
        captureId: Long,
        isFavorite: Boolean,
    ): Result<Unit>

    suspend fun updateBody(
        captureId: Long,
        body: String,
    ): Result<Unit>

    suspend fun deleteCaptures(captureIds: Set<Long>): Result<CaptureDeleteResult>

    suspend fun report(
        captureId: Long,
        reason: ReportReason,
        detail: String? = null,
    ): Result<Unit>
}

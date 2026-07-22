package com.chalkak.recap.core.data.capture

import com.chalkak.recap.core.model.capture.CaptureDeleteResult

interface CaptureMutationRepository {
    suspend fun updateFavorite(
        captureId: Long,
        isFavorite: Boolean,
    ): Result<Unit>

    suspend fun deleteCaptures(captureIds: Set<Long>): Result<CaptureDeleteResult>
}

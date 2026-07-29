package com.chalkak.recap.core.data.capture

import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendModeStore
import com.chalkak.recap.core.model.capture.CaptureDeleteResult
import com.chalkak.recap.core.model.capture.ReportReason
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SwitchingCaptureMutationRepository @Inject constructor(
    private val screenshotBackendModeStore: ScreenshotBackendModeStore,
    private val mockCaptureMutationRepository: MockCaptureMutationRepository,
    private val remoteCaptureMutationRepository: RemoteCaptureMutationRepository,
) : CaptureMutationRepository {
    override suspend fun updateFavorite(
        captureId: Long,
        isFavorite: Boolean,
    ): Result<Unit> =
        resolveDelegate().updateFavorite(captureId = captureId, isFavorite = isFavorite)

    override suspend fun updateBody(
        captureId: Long,
        body: String,
    ): Result<Unit> =
        resolveDelegate().updateBody(captureId = captureId, body = body)

    override suspend fun deleteCaptures(captureIds: Set<Long>): Result<CaptureDeleteResult> =
        resolveDelegate().deleteCaptures(captureIds)

    override suspend fun report(
        captureId: Long,
        reason: ReportReason,
        detail: String?,
    ): Result<Unit> =
        resolveDelegate().report(captureId = captureId, reason = reason, detail = detail)

    private suspend fun resolveDelegate(): CaptureMutationRepository {
        return when (screenshotBackendModeStore.currentMode()) {
            ScreenshotBackendMode.MOCK -> mockCaptureMutationRepository
            ScreenshotBackendMode.REMOTE -> remoteCaptureMutationRepository
        }
    }
}

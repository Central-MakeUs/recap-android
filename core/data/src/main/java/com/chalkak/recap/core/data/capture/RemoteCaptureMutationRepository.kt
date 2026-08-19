package com.chalkak.recap.core.data.capture

import com.chalkak.recap.core.data.capture.remote.BulkDeleteRequestDto
import com.chalkak.recap.core.data.capture.remote.CaptureApi
import com.chalkak.recap.core.data.capture.remote.CaptureUpdateRequestDto
import com.chalkak.recap.core.data.capture.remote.FavoriteRequestDto
import com.chalkak.recap.core.data.capture.remote.ReportRequestDto
import com.chalkak.recap.core.data.capture.remote.toCardTypeDto
import com.chalkak.recap.core.data.capture.remote.toDto
import com.chalkak.recap.core.data.network.runRemoteCatchingSuspend
import com.chalkak.recap.core.model.capture.CaptureDeleteResult
import com.chalkak.recap.core.model.capture.ReportReason
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteCaptureMutationRepository @Inject constructor(
    private val captureApi: CaptureApi,
    private val thumbnailCache: RemoteCaptureThumbnailCache,
    private val changeNotifier: CaptureChangeNotifier,
) : CaptureMutationRepository {
    override suspend fun updateFavorite(
        captureId: Long,
        isFavorite: Boolean,
    ): Result<Unit> {
        val result = runRemoteCatchingSuspend {
            captureApi.updateFavorite(
                captureId = captureId,
                body = FavoriteRequestDto(isFavorite = isFavorite),
            )
        }
        if (result.isSuccess) {
            changeNotifier.emit(CaptureChange.Upserted(setOf(captureId)))
        }
        return result
    }

    override suspend fun updateCapture(
        captureId: Long,
        title: String,
        summary: String,
        body: String,
        typeCode: ScreenshotContentType,
    ): Result<Unit> {
        val result = runRemoteCatchingSuspend {
            captureApi.update(
                captureId = captureId,
                body = CaptureUpdateRequestDto(
                    title = title,
                    summary = summary,
                    body = body,
                    cardType = typeCode.toCardTypeDto(),
                ),
            )
        }
        if (result.isSuccess) {
            changeNotifier.emit(CaptureChange.Upserted(setOf(captureId)))
        }
        return result
    }

    override suspend fun deleteCaptures(captureIds: Set<Long>): Result<CaptureDeleteResult> {
        if (captureIds.isEmpty()) {
            return Result.success(
                CaptureDeleteResult(
                    deletedIds = emptySet(),
                    failedIds = emptySet(),
                ),
            )
        }
        return try {
            val deleteResult = runRemoteCatchingSuspend {
                if (captureIds.size == 1) {
                    captureApi.delete(captureIds.first())
                } else {
                    captureApi.bulkDelete(
                        BulkDeleteRequestDto(captureIds = captureIds.toList()),
                    )
                }
            }
            if (deleteResult.isSuccess) {
                thumbnailCache.deleteCachedThumbnails(captureIds)
                changeNotifier.emit(CaptureChange.Deleted(captureIds))
                Result.success(
                    CaptureDeleteResult(
                        deletedIds = captureIds,
                        failedIds = emptySet(),
                    ),
                )
            } else {
                Result.success(
                    CaptureDeleteResult(
                        deletedIds = emptySet(),
                        failedIds = captureIds,
                    ),
                )
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        }
    }

    override suspend fun report(
        captureId: Long,
        reason: ReportReason,
        detail: String?,
    ): Result<Unit> =
        runRemoteCatchingSuspend {
            captureApi.report(
                captureId = captureId,
                body = ReportRequestDto(
                    reason = reason.toDto(),
                    detail = detail?.takeIf { it.isNotBlank() },
                ),
            )
        }
}

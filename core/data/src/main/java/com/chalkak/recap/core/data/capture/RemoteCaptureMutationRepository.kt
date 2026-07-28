package com.chalkak.recap.core.data.capture

import com.chalkak.recap.core.data.capture.remote.BodyUpdateRequestDto
import com.chalkak.recap.core.data.capture.remote.BulkDeleteRequestDto
import com.chalkak.recap.core.data.capture.remote.CaptureApi
import com.chalkak.recap.core.data.capture.remote.FavoriteRequestDto
import com.chalkak.recap.core.data.network.runRemoteCatchingSuspend
import com.chalkak.recap.core.model.capture.CaptureDeleteResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class RemoteCaptureMutationRepository @Inject constructor(
    private val captureApi: CaptureApi,
    private val thumbnailCache: RemoteCaptureThumbnailCache,
    private val changeNotifier: RemoteCaptureChangeNotifier,
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
            changeNotifier.notifyCaptureChanged()
        }
        return result
    }

    override suspend fun updateBody(
        captureId: Long,
        body: String,
    ): Result<Unit> {
        val result = runRemoteCatchingSuspend {
            captureApi.updateBody(
                captureId = captureId,
                body = BodyUpdateRequestDto(body = body),
            )
        }
        if (result.isSuccess) {
            changeNotifier.notifyCaptureChanged()
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
                changeNotifier.notifyCaptureChanged()
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
}

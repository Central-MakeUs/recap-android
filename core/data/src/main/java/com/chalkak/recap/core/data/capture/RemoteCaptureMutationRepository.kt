package com.chalkak.recap.core.data.capture

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
            val deletedIds = linkedSetOf<Long>()
            val failedIds = linkedSetOf<Long>()
            for (captureId in captureIds) {
                val deleteResult = runRemoteCatchingSuspend {
                    captureApi.delete(captureId)
                }
                if (deleteResult.isSuccess) {
                    deletedIds += captureId
                } else {
                    failedIds += captureId
                }
            }
            if (deletedIds.isNotEmpty()) {
                thumbnailCache.deleteCachedThumbnails(deletedIds)
                changeNotifier.notifyCaptureChanged()
            }
            Result.success(
                CaptureDeleteResult(
                    deletedIds = deletedIds,
                    failedIds = failedIds,
                ),
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        }
    }
}

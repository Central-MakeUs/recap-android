package com.chalkak.recap.core.data.capture

import androidx.annotation.VisibleForTesting
import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.model.capture.CaptureDeleteResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

@Singleton
class MockCaptureMutationRepository @Inject constructor(
    private val screenshotCardRepository: ScreenshotCardRepository,
    private val screenshotImageStorage: ScreenshotImageStorage,
) : CaptureMutationRepository {
    @VisibleForTesting
    var ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    override suspend fun updateFavorite(
        captureId: Long,
        isFavorite: Boolean,
    ): Result<Unit> =
        runCatching {
            screenshotCardRepository.updateFavorite(
                captureId = captureId,
                isFavorite = isFavorite,
            )
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
            screenshotCardRepository.deleteCards(captureIds)
            try {
                withContext(ioDispatcher + NonCancellable) {
                    screenshotImageStorage.deleteStoredImages(captureIds)
                }
            } catch (_: Exception) {
                // Room is already committed; private file cleanup remains best-effort.
            }
            Result.success(
                CaptureDeleteResult(
                    deletedIds = captureIds,
                    failedIds = emptySet(),
                ),
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}

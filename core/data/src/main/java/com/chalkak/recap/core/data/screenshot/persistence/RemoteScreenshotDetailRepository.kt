package com.chalkak.recap.core.data.screenshot.persistence

import com.chalkak.recap.core.data.capture.CaptureRepository
import com.chalkak.recap.core.data.capture.CaptureChange
import com.chalkak.recap.core.data.capture.CaptureChangeNotifier
import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.capture.toStoredScreenshotCard
import com.chalkak.recap.core.data.network.RemoteApiException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart

@Singleton
class RemoteScreenshotDetailRepository @Inject constructor(
    private val captureRepository: CaptureRepository,
    private val thumbnailCache: RemoteCaptureThumbnailCache,
    private val changeNotifier: CaptureChangeNotifier,
) : ScreenshotDetailRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCard(captureId: Long): Flow<StoredScreenshotCard?> {
        return changeNotifier.changes
            .onStart { emit(CaptureChange.Invalidated) }
            .mapLatest { fetchCard(captureId) }
    }

    private suspend fun fetchCard(captureId: Long): StoredScreenshotCard? {
        val result = captureRepository.getDetail(captureId)
        val detail = result.getOrElse { error ->
            if (error.isCaptureNotFound()) {
                return null
            }
            throw error
        }
        val thumbnailPath = thumbnailCache.resolveThumbnailSource(
            captureId = detail.captureId,
            remoteUrl = detail.originalImageUrl,
        )
        return detail.toStoredScreenshotCard(thumbnailPath = thumbnailPath)
    }
}

internal fun Throwable.isCaptureNotFound(): Boolean {
    val code = (this as? RemoteApiException)?.code ?: return false
    return code.equals("NOT_FOUND", ignoreCase = true) ||
        code.equals("HTTP_404", ignoreCase = true)
}

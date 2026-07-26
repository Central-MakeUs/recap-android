package com.chalkak.recap.core.data.screenshot.persistence

import com.chalkak.recap.core.data.capture.CaptureRepository
import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.network.RemoteApiException
import com.chalkak.recap.core.data.network.RemoteNetworkException
import com.chalkak.recap.core.model.capture.CaptureDetail
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RemoteScreenshotDetailRepositoryTest {
    private val captureRepository = mockk<CaptureRepository>()
    private val thumbnailCache = mockk<RemoteCaptureThumbnailCache>()
    private val changeNotifier = RemoteCaptureChangeNotifier()

    private val repository = RemoteScreenshotDetailRepository(
        captureRepository = captureRepository,
        thumbnailCache = thumbnailCache,
        changeNotifier = changeNotifier,
    )

    @Test
    fun `observeCard emits mapped card on success`() = runTest {
        coEvery { captureRepository.getDetail(10L) } returns Result.success(
            CaptureDetail(
                captureId = 10L,
                typeCode = ScreenshotContentType.SHOPPING,
                title = "cable",
                summary = "hdmi",
                body = "body",
                originalImageUrl = "https://cdn.example/10.jpg",
                isFavorite = false,
                organizedAt = "2026-07-19T00:00:00Z",
            ),
        )
        coEvery {
            thumbnailCache.resolveThumbnailSource(10L, "https://cdn.example/10.jpg")
        } returns "/thumbs/10.jpg"

        val card = repository.observeCard(10L).first()

        assertEquals(10L, card?.analysisResult?.captureId)
        assertEquals("cable", card?.analysisResult?.title)
        assertEquals("https://cdn.example/10.jpg", card?.imageRefs?.sourceImageUri)
        assertEquals("/thumbs/10.jpg", card?.imageRefs?.thumbnailPath)
    }

    @Test
    fun `observeCard emits null for not found`() = runTest {
        coEvery { captureRepository.getDetail(99L) } returns Result.failure(
            RemoteApiException(code = "NOT_FOUND", message = "missing"),
        )

        assertNull(repository.observeCard(99L).first())
    }

    @Test
    fun `observeCard throws for network failure`() = runTest {
        coEvery { captureRepository.getDetail(1L) } returns Result.failure(RemoteNetworkException())

        assertThrows<RemoteNetworkException> {
            repository.observeCard(1L).first()
        }
    }

    @Test
    fun `isCaptureNotFound recognizes http 404`() {
        assertTrue(RemoteApiException(code = "HTTP_404", message = "gone").isCaptureNotFound())
        assertTrue(RemoteApiException(code = "not_found", message = "gone").isCaptureNotFound())
    }
}

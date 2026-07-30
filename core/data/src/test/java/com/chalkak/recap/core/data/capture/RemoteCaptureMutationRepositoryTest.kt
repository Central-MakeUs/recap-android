package com.chalkak.recap.core.data.capture

import com.chalkak.recap.core.data.capture.remote.BulkDeleteRequestDto
import com.chalkak.recap.core.data.capture.remote.CaptureApi
import com.chalkak.recap.core.data.capture.remote.CaptureUpdateRequestDto
import com.chalkak.recap.core.data.capture.remote.CardTypeDto
import com.chalkak.recap.core.data.capture.remote.ReportReasonDto
import com.chalkak.recap.core.data.capture.remote.ReportRequestDto
import com.chalkak.recap.core.data.network.RemoteApiException
import com.chalkak.recap.core.model.capture.CaptureDeleteResult
import com.chalkak.recap.core.model.capture.ReportReason
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RemoteCaptureMutationRepositoryTest {
    private val captureApi = mockk<CaptureApi>()
    private val thumbnailCache = mockk<RemoteCaptureThumbnailCache>()
    private val changeNotifier = mockk<RemoteCaptureChangeNotifier>()

    @Test
    fun `empty delete returns empty success result`() = runTest {
        val repository = createRepository()

        val result = repository.deleteCaptures(emptySet())

        assertEquals(
            CaptureDeleteResult(deletedIds = emptySet(), failedIds = emptySet()),
            result.getOrThrow(),
        )
        coVerify(exactly = 0) { captureApi.delete(any()) }
        coVerify(exactly = 0) { captureApi.bulkDelete(any()) }
        verify(exactly = 0) { thumbnailCache.deleteCachedThumbnails(any()) }
        verify(exactly = 0) { changeNotifier.notifyCaptureChanged() }
    }

    @Test
    fun `single delete uses delete api and succeeds`() = runTest {
        coEvery { captureApi.delete(1L) } returns Unit
        every { thumbnailCache.deleteCachedThumbnails(any()) } just Runs
        every { changeNotifier.notifyCaptureChanged() } just Runs
        val repository = createRepository()

        val result = repository.deleteCaptures(setOf(1L)).getOrThrow()

        assertEquals(setOf(1L), result.deletedIds)
        assertTrue(result.failedIds.isEmpty())
        coVerify(exactly = 1) { captureApi.delete(1L) }
        coVerify(exactly = 0) { captureApi.bulkDelete(any()) }
        verify(exactly = 1) { thumbnailCache.deleteCachedThumbnails(setOf(1L)) }
        verify(exactly = 1) { changeNotifier.notifyCaptureChanged() }
    }

    @Test
    fun `single delete failure marks id as failed`() = runTest {
        coEvery { captureApi.delete(1L) } throws RemoteApiException(code = "ERR", message = "fail")
        val repository = createRepository()

        val result = repository.deleteCaptures(setOf(1L)).getOrThrow()

        assertTrue(result.deletedIds.isEmpty())
        assertEquals(setOf(1L), result.failedIds)
        coVerify(exactly = 1) { captureApi.delete(1L) }
        coVerify(exactly = 0) { captureApi.bulkDelete(any()) }
        verify(exactly = 0) { thumbnailCache.deleteCachedThumbnails(any()) }
        verify(exactly = 0) { changeNotifier.notifyCaptureChanged() }
    }

    @Test
    fun `bulk delete failure marks all ids as failed`() = runTest {
        coEvery { captureApi.bulkDelete(any()) } throws RemoteApiException(code = "ERR", message = "fail")
        val repository = createRepository()

        val result = repository.deleteCaptures(setOf(1L, 2L, 3L)).getOrThrow()

        assertTrue(result.deletedIds.isEmpty())
        assertEquals(setOf(1L, 2L, 3L), result.failedIds)
        coVerify(exactly = 0) { captureApi.delete(any()) }
        coVerify(exactly = 1) {
            captureApi.bulkDelete(BulkDeleteRequestDto(captureIds = listOf(1L, 2L, 3L)))
        }
        verify(exactly = 0) { thumbnailCache.deleteCachedThumbnails(any()) }
        verify(exactly = 0) { changeNotifier.notifyCaptureChanged() }
    }

    @Test
    fun `bulk success deletes cache once and notifies once`() = runTest {
        coEvery { captureApi.bulkDelete(any()) } returns Unit
        every { thumbnailCache.deleteCachedThumbnails(any()) } just Runs
        every { changeNotifier.notifyCaptureChanged() } just Runs
        val repository = createRepository()

        val result = repository.deleteCaptures(setOf(1L, 2L)).getOrThrow()

        assertEquals(setOf(1L, 2L), result.deletedIds)
        assertTrue(result.failedIds.isEmpty())
        coVerify(exactly = 0) { captureApi.delete(any()) }
        coVerify(exactly = 1) {
            captureApi.bulkDelete(BulkDeleteRequestDto(captureIds = listOf(1L, 2L)))
        }
        verify(exactly = 1) { thumbnailCache.deleteCachedThumbnails(setOf(1L, 2L)) }
        verify(exactly = 1) { changeNotifier.notifyCaptureChanged() }
    }

    @Test
    fun `updateCapture success notifies capture changed`() = runTest {
        coEvery {
            captureApi.update(
                1L,
                CaptureUpdateRequestDto(
                    title = "title",
                    summary = "summary",
                    body = "new body",
                    cardType = CardTypeDto.JOB,
                ),
            )
        } returns Unit
        every { changeNotifier.notifyCaptureChanged() } just Runs
        val repository = createRepository()

        val result = repository.updateCapture(
            captureId = 1L,
            title = "title",
            summary = "summary",
            body = "new body",
            typeCode = ScreenshotContentType.JOB,
        )

        assertTrue(result.isSuccess)
        verify(exactly = 1) { changeNotifier.notifyCaptureChanged() }
    }

    @Test
    fun `updateCapture failure skips notifier`() = runTest {
        coEvery { captureApi.update(any(), any()) } throws RemoteApiException(code = "ERR", message = "fail")
        val repository = createRepository()

        val result = repository.updateCapture(
            captureId = 1L,
            title = "title",
            summary = "summary",
            body = "new body",
            typeCode = ScreenshotContentType.JOB,
        )

        assertTrue(result.isFailure)
        verify(exactly = 0) { changeNotifier.notifyCaptureChanged() }
    }

    @Test
    fun `cancellation is rethrown`() = runTest {
        coEvery { captureApi.bulkDelete(any()) } throws CancellationException("cancelled")
        val repository = createRepository()

        assertThrows<CancellationException> {
            repository.deleteCaptures(setOf(1L, 2L))
        }
        coVerify(exactly = 0) { captureApi.delete(any()) }
        verify(exactly = 0) { thumbnailCache.deleteCachedThumbnails(any()) }
        verify(exactly = 0) { changeNotifier.notifyCaptureChanged() }
    }

    @Test
    fun `single delete cancellation is rethrown`() = runTest {
        coEvery { captureApi.delete(1L) } throws CancellationException("cancelled")
        val repository = createRepository()

        assertThrows<CancellationException> {
            repository.deleteCaptures(setOf(1L))
        }
        coVerify(exactly = 0) { captureApi.bulkDelete(any()) }
        verify(exactly = 0) { thumbnailCache.deleteCachedThumbnails(any()) }
        verify(exactly = 0) { changeNotifier.notifyCaptureChanged() }
    }

    @Test
    fun `report sends reason and detail`() = runTest {
        coEvery {
            captureApi.report(
                10L,
                ReportRequestDto(
                    reason = ReportReasonDto.OTHER,
                    detail = "기타 사유",
                ),
            )
        } returns Unit
        val repository = createRepository()

        val result = repository.report(
            captureId = 10L,
            reason = ReportReason.OTHER,
            detail = "기타 사유",
        )

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            captureApi.report(
                10L,
                ReportRequestDto(
                    reason = ReportReasonDto.OTHER,
                    detail = "기타 사유",
                ),
            )
        }
        verify(exactly = 0) { changeNotifier.notifyCaptureChanged() }
    }

    private fun createRepository(): RemoteCaptureMutationRepository {
        return RemoteCaptureMutationRepository(
            captureApi = captureApi,
            thumbnailCache = thumbnailCache,
            changeNotifier = changeNotifier,
        )
    }
}

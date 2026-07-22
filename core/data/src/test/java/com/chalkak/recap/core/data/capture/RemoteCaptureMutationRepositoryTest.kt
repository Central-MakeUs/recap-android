package com.chalkak.recap.core.data.capture

import com.chalkak.recap.core.data.capture.remote.CaptureApi
import com.chalkak.recap.core.data.network.RemoteApiException
import com.chalkak.recap.core.model.capture.CaptureDeleteResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
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
        verify(exactly = 0) { thumbnailCache.deleteCachedThumbnails(any()) }
        verify(exactly = 0) { changeNotifier.notifyCaptureChanged() }
    }

    @Test
    fun `partial delete continues after middle failure and cleans only successes`() = runTest {
        coEvery { captureApi.delete(1L) } returns Unit
        coEvery { captureApi.delete(2L) } throws RemoteApiException(code = "ERR", message = "fail")
        coEvery { captureApi.delete(3L) } returns Unit
        every { thumbnailCache.deleteCachedThumbnails(any()) } just Runs
        every { changeNotifier.notifyCaptureChanged() } just Runs
        val repository = createRepository()

        val result = repository.deleteCaptures(setOf(1L, 2L, 3L)).getOrThrow()

        assertEquals(setOf(1L, 3L), result.deletedIds)
        assertEquals(setOf(2L), result.failedIds)
        coVerify(exactly = 1) { captureApi.delete(1L) }
        coVerify(exactly = 1) { captureApi.delete(2L) }
        coVerify(exactly = 1) { captureApi.delete(3L) }
        verify(exactly = 1) { thumbnailCache.deleteCachedThumbnails(setOf(1L, 3L)) }
        verify(exactly = 1) { changeNotifier.notifyCaptureChanged() }
    }

    @Test
    fun `all failures skip cache cleanup and notifier`() = runTest {
        coEvery { captureApi.delete(any()) } throws RemoteApiException(code = "ERR", message = "fail")
        val repository = createRepository()

        val result = repository.deleteCaptures(setOf(1L, 2L)).getOrThrow()

        assertTrue(result.deletedIds.isEmpty())
        assertEquals(setOf(1L, 2L), result.failedIds)
        verify(exactly = 0) { thumbnailCache.deleteCachedThumbnails(any()) }
        verify(exactly = 0) { changeNotifier.notifyCaptureChanged() }
    }

    @Test
    fun `full success deletes cache once and notifies once`() = runTest {
        coEvery { captureApi.delete(any()) } returns Unit
        every { thumbnailCache.deleteCachedThumbnails(any()) } just Runs
        every { changeNotifier.notifyCaptureChanged() } just Runs
        val repository = createRepository()

        val result = repository.deleteCaptures(setOf(1L, 2L)).getOrThrow()

        assertEquals(setOf(1L, 2L), result.deletedIds)
        assertTrue(result.failedIds.isEmpty())
        verify(exactly = 1) { thumbnailCache.deleteCachedThumbnails(setOf(1L, 2L)) }
        verify(exactly = 1) { changeNotifier.notifyCaptureChanged() }
        coVerifyOrder {
            captureApi.delete(1L)
            captureApi.delete(2L)
            thumbnailCache.deleteCachedThumbnails(setOf(1L, 2L))
            changeNotifier.notifyCaptureChanged()
        }
    }

    @Test
    fun `cancellation is rethrown`() = runTest {
        coEvery { captureApi.delete(1L) } throws CancellationException("cancelled")
        val repository = createRepository()

        assertThrows<CancellationException> {
            repository.deleteCaptures(setOf(1L, 2L))
        }
        coVerify(exactly = 0) { captureApi.delete(2L) }
        verify(exactly = 0) { thumbnailCache.deleteCachedThumbnails(any()) }
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

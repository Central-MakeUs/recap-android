package com.chalkak.recap.core.data.capture

import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.model.capture.CaptureDeleteResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MockCaptureMutationRepositoryTest {
    private val cardRepository = mockk<ScreenshotCardRepository>()
    private val imageStorage = mockk<ScreenshotImageStorage>()
    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `empty delete returns empty success result`() = runTest(testDispatcher) {
        val repository = createRepository()

        val result = repository.deleteCaptures(emptySet())

        assertEquals(
            CaptureDeleteResult(deletedIds = emptySet(), failedIds = emptySet()),
            result.getOrThrow(),
        )
        coVerify(exactly = 0) { cardRepository.deleteCards(any()) }
    }

    @Test
    fun `successful delete returns all ids as deleted`() = runTest(testDispatcher) {
        coEvery { cardRepository.deleteCards(setOf(1L, 2L)) } returns Unit
        every { imageStorage.deleteStoredImages(setOf(1L, 2L)) } just Runs
        val repository = createRepository()

        val result = repository.deleteCaptures(setOf(1L, 2L)).getOrThrow()

        assertEquals(setOf(1L, 2L), result.deletedIds)
        assertTrue(result.failedIds.isEmpty())
        verify(exactly = 1) { imageStorage.deleteStoredImages(setOf(1L, 2L)) }
    }

    @Test
    fun `room delete failure returns Result failure and skips file cleanup`() = runTest(testDispatcher) {
        coEvery { cardRepository.deleteCards(any()) } throws IllegalStateException("db fail")
        val repository = createRepository()

        val result = repository.deleteCaptures(setOf(1L))

        assertTrue(result.isFailure)
        verify(exactly = 0) { imageStorage.deleteStoredImages(any()) }
    }

    @Test
    fun `file cleanup failure still returns deleted ids`() = runTest(testDispatcher) {
        coEvery { cardRepository.deleteCards(setOf(1L)) } returns Unit
        every { imageStorage.deleteStoredImages(setOf(1L)) } throws RuntimeException("io fail")
        val repository = createRepository()

        val result = repository.deleteCaptures(setOf(1L)).getOrThrow()

        assertEquals(setOf(1L), result.deletedIds)
        assertTrue(result.failedIds.isEmpty())
    }

    @Test
    fun `updateCapture updates existing card content`() = runTest(testDispatcher) {
        coEvery {
            cardRepository.updateCardContent(
                captureId = 1L,
                title = "new title",
                summary = "new summary",
                body = "new body",
                typeCode = ScreenshotContentType.SCHEDULE,
                updatedAtMillis = any(),
            )
        } returns true
        val repository = createRepository()

        val result = repository.updateCapture(
            captureId = 1L,
            title = "new title",
            summary = "new summary",
            body = "new body",
            typeCode = ScreenshotContentType.SCHEDULE,
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateCapture fails when card is missing`() = runTest(testDispatcher) {
        coEvery {
            cardRepository.updateCardContent(
                captureId = 1L,
                title = any(),
                summary = any(),
                body = any(),
                typeCode = any(),
                updatedAtMillis = any(),
            )
        } returns false
        val repository = createRepository()

        val result = repository.updateCapture(
            captureId = 1L,
            title = "title",
            summary = "summary",
            body = "body",
            typeCode = ScreenshotContentType.JOB,
        )

        assertTrue(result.isFailure)
        coVerify(exactly = 1) {
            cardRepository.updateCardContent(
                captureId = 1L,
                title = "title",
                summary = "summary",
                body = "body",
                typeCode = ScreenshotContentType.JOB,
                updatedAtMillis = any(),
            )
        }
    }

    @Test
    fun `cancellation is rethrown`() = runTest(testDispatcher) {
        coEvery { cardRepository.deleteCards(any()) } throws CancellationException("cancelled")
        val repository = createRepository()

        assertThrows<CancellationException> {
            repository.deleteCaptures(setOf(1L))
        }
    }

    private fun createRepository(): MockCaptureMutationRepository {
        return MockCaptureMutationRepository(
            screenshotCardRepository = cardRepository,
            screenshotImageStorage = imageStorage,
        ).apply {
            ioDispatcher = testDispatcher
        }
    }
}

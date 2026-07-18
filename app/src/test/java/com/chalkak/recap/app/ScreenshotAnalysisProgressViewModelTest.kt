package com.chalkak.recap.app

import android.net.Uri
import com.chalkak.recap.core.data.screenshot.ScreenshotAnalysisInput
import com.chalkak.recap.core.data.screenshot.ScreenshotAnalysisRepository
import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardImageRefs
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class ScreenshotAnalysisProgressViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<ScreenshotAnalysisRepository>()
    private val screenshotCardRepository = mockk<ScreenshotCardRepository>(relaxed = true)
    private val screenshotImageStorage = mockk<ScreenshotImageStorage>(relaxed = true)
    private lateinit var viewModel: ScreenshotAnalysisProgressViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ScreenshotAnalysisProgressViewModel(
            screenshotAnalysisRepository = repository,
            screenshotCardRepository = screenshotCardRepository,
            screenshotImageStorage = screenshotImageStorage,
        ).apply {
            ioDispatcher = testDispatcher
        }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `starts at idle state`() {
        val state = viewModel.uiState.value

        assertFalse(state.isRunning)
        assertEquals(0, state.completedCount)
        assertEquals(0, state.totalCount)
        assertEquals(0f, state.progress)
        assertTrue(state.results.isEmpty())
    }

    @Test
    fun `startMockAnalysis sets running state and total count`() = runTest(testDispatcher) {
        every { repository.analyze(any<ScreenshotAnalysisInput>()) } returns analysisResult(1L)

        viewModel.startMockAnalysis(sampleImages(count = 2))
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.isRunning)
        assertEquals(2, state.totalCount)
        assertEquals(0, state.completedCount)
        assertEquals(0f, state.progress)
    }

    @Test
    fun `advancing time by 500ms per image increments completed count`() = runTest(testDispatcher) {
        every { repository.analyze(any<ScreenshotAnalysisInput>()) } returns analysisResult(1L)

        viewModel.startMockAnalysis(sampleImages(count = 2))
        runCurrent()

        advanceTimeBy(500.milliseconds)
        runCurrent()
        assertEquals(1, viewModel.uiState.value.completedCount)
        assertEquals(0.5f, viewModel.uiState.value.progress)

        advanceTimeBy(500.milliseconds)
        runCurrent()
        assertEquals(2, viewModel.uiState.value.completedCount)
        assertEquals(1f, viewModel.uiState.value.progress)
        assertFalse(viewModel.uiState.value.isRunning)
    }

    @Test
    fun `repository inputs use selected image display names in order`() = runTest(testDispatcher) {
        every { repository.analyze(any<ScreenshotAnalysisInput>()) } returns analysisResult(1L)

        val images = listOf(
            LocalImage(uri = "content://1", displayName = "first.png", dateAddedMillis = 1L),
            LocalImage(uri = "content://2", displayName = "second.png", dateAddedMillis = 2L),
        )

        viewModel.startMockAnalysis(images)
        runCurrent()
        advanceTimeBy(500.milliseconds)
        runCurrent()
        advanceTimeBy(500.milliseconds)
        runCurrent()

        verify(exactly = 1) { repository.analyze(ScreenshotAnalysisInput(fileName = "first.png")) }
        verify(exactly = 1) { repository.analyze(ScreenshotAnalysisInput(fileName = "second.png")) }
    }

    @Test
    fun `starting a second job cancels and resets the previous job`() = runTest(testDispatcher) {
        every { repository.analyze(any<ScreenshotAnalysisInput>()) } returns analysisResult(1L)

        viewModel.startMockAnalysis(sampleImages(count = 3))
        runCurrent()
        advanceTimeBy(500.milliseconds)
        runCurrent()
        assertEquals(1, viewModel.uiState.value.completedCount)

        viewModel.startMockAnalysis(sampleImages(count = 1))
        runCurrent()
        assertEquals(1, viewModel.uiState.value.totalCount)
        assertEquals(0, viewModel.uiState.value.completedCount)
        assertEquals(0f, viewModel.uiState.value.progress)

        advanceTimeBy(500.milliseconds)
        runCurrent()
        assertEquals(1, viewModel.uiState.value.completedCount)
        assertEquals(1f, viewModel.uiState.value.progress)
        assertFalse(viewModel.uiState.value.isRunning)
    }

    @Test
    fun `saves each analysis result with image refs while preserving progress`() = runTest(testDispatcher) {
        mockkStatic(Uri::class)
        val firstUri = mockk<Uri>()
        val secondUri = mockk<Uri>()
        every { Uri.parse("content://1") } returns firstUri
        every { Uri.parse("content://2") } returns secondUri

        val firstResult = analysisResult(captureId = 1L)
        val secondResult = analysisResult(captureId = 2L)
        every { repository.analyze(ScreenshotAnalysisInput(fileName = "image_1.png")) } returns firstResult
        every { repository.analyze(ScreenshotAnalysisInput(fileName = "image_2.png")) } returns secondResult
        every {
            screenshotImageStorage.copyImageFromUri(1L, firstUri)
        } returns "/files/1"
        every {
            screenshotImageStorage.copyImageFromUri(2L, secondUri)
        } returns null
        every {
            screenshotImageStorage.createThumbnailFromStoredImage(1L)
        } returns "/thumbs/1.jpg"
        every {
            screenshotImageStorage.createThumbnailFromUri(2L, secondUri)
        } returns null
        coEvery {
            screenshotCardRepository.saveAnalysisResults(any(), any())
        } returns Unit

        viewModel.startMockAnalysis(sampleImages(count = 2))
        runCurrent()
        advanceTimeBy(500.milliseconds)
        runCurrent()
        advanceTimeBy(500.milliseconds)
        runCurrent()

        coVerify(exactly = 1) {
            screenshotCardRepository.saveAnalysisResults(
                results = listOf(firstResult),
                imageRefsByCaptureId = mapOf(
                    1L to ScreenshotCardImageRefs(
                        sourceImageUri = "content://1",
                        storedImagePath = "/files/1",
                        thumbnailPath = "/thumbs/1.jpg",
                    ),
                ),
            )
        }
        coVerify(exactly = 1) {
            screenshotCardRepository.saveAnalysisResults(
                results = listOf(secondResult),
                imageRefsByCaptureId = mapOf(
                    2L to ScreenshotCardImageRefs(
                        sourceImageUri = "content://2",
                        storedImagePath = null,
                        thumbnailPath = null,
                    ),
                ),
            )
        }
        assertEquals(2, viewModel.uiState.value.completedCount)
        assertFalse(viewModel.uiState.value.isRunning)

        unmockkStatic(Uri::class)
    }

    @Test
    fun `thumbnail creation failure still saves analysis with available image refs`() = runTest(testDispatcher) {
        mockkStatic(Uri::class)
        val firstUri = mockk<Uri>()
        every { Uri.parse("content://1") } returns firstUri

        val firstResult = analysisResult(captureId = 1L)
        every { repository.analyze(ScreenshotAnalysisInput(fileName = "image_1.png")) } returns firstResult
        every {
            screenshotImageStorage.copyImageFromUri(1L, firstUri)
        } returns "/files/1"
        every {
            screenshotImageStorage.createThumbnailFromStoredImage(1L)
        } returns null
        coEvery {
            screenshotCardRepository.saveAnalysisResults(any(), any())
        } returns Unit

        viewModel.startMockAnalysis(sampleImages(count = 1))
        runCurrent()
        advanceTimeBy(500.milliseconds)
        runCurrent()

        coVerify(exactly = 1) {
            screenshotCardRepository.saveAnalysisResults(
                results = listOf(firstResult),
                imageRefsByCaptureId = mapOf(
                    1L to ScreenshotCardImageRefs(
                        sourceImageUri = "content://1",
                        storedImagePath = "/files/1",
                        thumbnailPath = null,
                    ),
                ),
            )
        }
        assertEquals(1, viewModel.uiState.value.completedCount)
        assertFalse(viewModel.uiState.value.isRunning)

        unmockkStatic(Uri::class)
    }

    @Test
    fun `thumbnail success path passes absolute path to repository`() = runTest(testDispatcher) {
        mockkStatic(Uri::class)
        val firstUri = mockk<Uri>()
        every { Uri.parse("content://1") } returns firstUri

        val firstResult = analysisResult(captureId = 1L)
        every { repository.analyze(ScreenshotAnalysisInput(fileName = "image_1.png")) } returns firstResult
        every {
            screenshotImageStorage.copyImageFromUri(1L, firstUri)
        } returns null
        every {
            screenshotImageStorage.createThumbnailFromUri(1L, firstUri)
        } returns "/files/recap/thumbnails/1.jpg"
        coEvery {
            screenshotCardRepository.saveAnalysisResults(any(), any())
        } returns Unit

        viewModel.startMockAnalysis(sampleImages(count = 1))
        runCurrent()
        advanceTimeBy(500.milliseconds)
        runCurrent()

        coVerify(exactly = 1) {
            screenshotCardRepository.saveAnalysisResults(
                results = listOf(firstResult),
                imageRefsByCaptureId = mapOf(
                    1L to ScreenshotCardImageRefs(
                        sourceImageUri = "content://1",
                        storedImagePath = null,
                        thumbnailPath = "/files/recap/thumbnails/1.jpg",
                    ),
                ),
            )
        }
        assertEquals(1, viewModel.uiState.value.completedCount)

        unmockkStatic(Uri::class)
    }

    @Test
    fun `repository save failure exposes error state and skips completed progress`() = runTest(testDispatcher) {
        mockkStatic(Uri::class)
        val firstUri = mockk<Uri>()
        val secondUri = mockk<Uri>()
        every { Uri.parse("content://1") } returns firstUri
        every { Uri.parse("content://2") } returns secondUri

        val firstResult = analysisResult(captureId = 1L)
        val secondResult = analysisResult(captureId = 2L)
        every { repository.analyze(ScreenshotAnalysisInput(fileName = "image_1.png")) } returns firstResult
        every { repository.analyze(ScreenshotAnalysisInput(fileName = "image_2.png")) } returns secondResult
        every { screenshotImageStorage.copyImageFromUri(any(), any()) } returns "/files/image"
        coEvery {
            screenshotCardRepository.saveAnalysisResults(
                results = listOf(firstResult),
                imageRefsByCaptureId = any(),
            )
        } throws RuntimeException("room failure")
        coEvery {
            screenshotCardRepository.saveAnalysisResults(
                results = listOf(secondResult),
                imageRefsByCaptureId = any(),
            )
        } returns Unit

        viewModel.startMockAnalysis(sampleImages(count = 2))
        runCurrent()
        advanceTimeBy(500.milliseconds)
        runCurrent()
        advanceTimeBy(500.milliseconds)
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals("Failed to save screenshot analysis result", state.errorMessage)
        assertEquals(1, state.completedCount)
        assertEquals(0.5f, state.progress)
        assertEquals(listOf(secondResult), state.results)
        assertFalse(state.isRunning)

        unmockkStatic(Uri::class)
    }

    private fun sampleImages(count: Int): List<LocalImage> {
        return (1..count).map { index ->
            LocalImage(
                uri = "content://$index",
                displayName = "image_$index.png",
                dateAddedMillis = index.toLong(),
            )
        }
    }

    private fun analysisResult(captureId: Long): ScreenshotAnalysisResult {
        return ScreenshotAnalysisResult(
            captureId = captureId,
            typeCode = ScreenshotContentType.ETC,
            title = "title-$captureId",
            summary = "summary-$captureId",
            body = "body-$captureId",
            originalImageUrl = "mock://captures/$captureId",
            isFavorite = false,
            organizedAt = Instant.ofEpochMilli(1000L),
        )
    }
}

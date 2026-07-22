package com.chalkak.recap.app

import android.net.Uri
import com.chalkak.recap.core.data.screenshot.ScreenshotAnalysisInput
import com.chalkak.recap.core.data.screenshot.ScreenshotAnalysisRepository
import com.chalkak.recap.core.data.screenshot.ScreenshotAnalysisRunState
import com.chalkak.recap.core.data.screenshot.ScreenshotOrganizeOutcome
import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardImageRefs
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.core.model.capture.OrganizeStatus
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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

@OptIn(ExperimentalCoroutinesApi::class)
class ScreenshotAnalysisProgressViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<ScreenshotAnalysisRepository>()
    private val screenshotCardRepository = mockk<ScreenshotCardRepository>(relaxed = true)
    private val screenshotImageStorage = mockk<ScreenshotImageStorage>(relaxed = true)
    private val screenshotAnalysisRunState = ScreenshotAnalysisRunState()
    private lateinit var viewModel: ScreenshotAnalysisProgressViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ScreenshotAnalysisProgressViewModel(
            screenshotAnalysisRepository = repository,
            screenshotCardRepository = screenshotCardRepository,
            screenshotImageStorage = screenshotImageStorage,
            screenshotAnalysisRunState = screenshotAnalysisRunState,
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
        assertFalse(screenshotAnalysisRunState.isRunning.value)
    }

    @Test
    fun `startAnalysis sets running state and total count`() = runTest(testDispatcher) {
        coEvery { repository.organize(any(), any()) } coAnswers {
            val onProgress = secondArg<(Int, Int) -> Unit>()
            onProgress(0, 2)
            kotlinx.coroutines.awaitCancellation()
        }

        viewModel.startAnalysis(sampleImages(count = 2))
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.isRunning)
        assertEquals(2, state.totalCount)
        assertEquals(0, state.completedCount)
        assertEquals(0f, state.progress)
        assertTrue(screenshotAnalysisRunState.isRunning.value)
    }

    @Test
    fun `local organize progress updates completed count`() = runTest(testDispatcher) {
        val first = analysisResult(1L)
        val second = analysisResult(2L)
        coEvery { repository.organize(any(), any()) } coAnswers {
            val onProgress = secondArg<(Int, Int) -> Unit>()
            onProgress(1, 2)
            onProgress(2, 2)
            ScreenshotOrganizeOutcome.LocalResults(listOf(first, second))
        }

        viewModel.startAnalysis(sampleImages(count = 2))
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(2, state.completedCount)
        assertEquals(1f, state.progress)
        assertFalse(state.isRunning)
        assertEquals(listOf(first, second), state.results)
        assertFalse(screenshotAnalysisRunState.isRunning.value)
    }

    @Test
    fun `repository inputs use selected image display names and uris`() = runTest(testDispatcher) {
        coEvery { repository.organize(any(), any()) } returns ScreenshotOrganizeOutcome.LocalResults(
            emptyList(),
        )

        val images = listOf(
            LocalImage(uri = "content://1", displayName = "first.png", dateAddedMillis = 1L),
            LocalImage(uri = "content://2", displayName = "second.png", dateAddedMillis = 2L),
        )

        viewModel.startAnalysis(images)
        runCurrent()

        coVerify(exactly = 1) {
            repository.organize(
                listOf(
                    ScreenshotAnalysisInput(fileName = "first.png", uri = "content://1"),
                    ScreenshotAnalysisInput(fileName = "second.png", uri = "content://2"),
                ),
                any(),
            )
        }
    }

    @Test
    fun `starting a second job cancels and resets the previous job`() = runTest(testDispatcher) {
        coEvery { repository.organize(any(), any()) } coAnswers {
            val inputs = firstArg<List<ScreenshotAnalysisInput>>()
            val onProgress = secondArg<(Int, Int) -> Unit>()
            onProgress(0, inputs.size)
            kotlinx.coroutines.awaitCancellation()
        }

        viewModel.startAnalysis(sampleImages(count = 3))
        runCurrent()
        assertEquals(3, viewModel.uiState.value.totalCount)

        viewModel.startAnalysis(sampleImages(count = 1))
        runCurrent()
        assertEquals(1, viewModel.uiState.value.totalCount)
        assertEquals(0, viewModel.uiState.value.completedCount)
        assertEquals(0f, viewModel.uiState.value.progress)
        assertTrue(screenshotAnalysisRunState.isRunning.value)
    }

    @Test
    fun `empty analysis restores idle run state`() = runTest(testDispatcher) {
        viewModel.startAnalysis(emptyList())
        runCurrent()

        assertFalse(viewModel.uiState.value.isRunning)
        assertFalse(screenshotAnalysisRunState.isRunning.value)
        coVerify(exactly = 0) { repository.organize(any(), any()) }
    }

    @Test
    fun `repository exception sets safe error and restores idle`() = runTest(testDispatcher) {
        coEvery { repository.organize(any(), any()) } throws RuntimeException("boom")

        viewModel.startAnalysis(sampleImages(count = 1))
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals("Failed to analyze screenshot", state.errorMessage)
        assertFalse(state.isRunning)
        assertFalse(screenshotAnalysisRunState.isRunning.value)
    }

    @Test
    fun `remote completed skips room persistence`() = runTest(testDispatcher) {
        coEvery { repository.organize(any(), any()) } coAnswers {
            val onProgress = secondArg<(Int, Int) -> Unit>()
            onProgress(2, 2)
            ScreenshotOrganizeOutcome.RemoteCompleted(
                successCount = 2,
                failCount = 0,
                status = OrganizeStatus.COMPLETED,
            )
        }

        viewModel.startAnalysis(sampleImages(count = 2))
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isRunning)
        assertEquals(2, state.completedCount)
        assertEquals(1f, state.progress)
        assertTrue(state.results.isEmpty())
        coVerify(exactly = 0) { screenshotCardRepository.saveAnalysisResults(any(), any()) }
        assertFalse(screenshotAnalysisRunState.isRunning.value)
    }

    @Test
    fun `saves each local analysis result with image refs`() = runTest(testDispatcher) {
        mockkStatic(Uri::class)
        val firstUri = mockk<Uri>()
        val secondUri = mockk<Uri>()
        every { Uri.parse("content://1") } returns firstUri
        every { Uri.parse("content://2") } returns secondUri

        val firstResult = analysisResult(captureId = 1L)
        val secondResult = analysisResult(captureId = 2L)
        coEvery { repository.organize(any(), any()) } returns ScreenshotOrganizeOutcome.LocalResults(
            listOf(firstResult, secondResult),
        )
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

        viewModel.startAnalysis(sampleImages(count = 2))
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
    fun `repository save failure exposes error state and stops`() = runTest(testDispatcher) {
        mockkStatic(Uri::class)
        val firstUri = mockk<Uri>()
        every { Uri.parse("content://1") } returns firstUri

        val firstResult = analysisResult(captureId = 1L)
        val secondResult = analysisResult(captureId = 2L)
        coEvery { repository.organize(any(), any()) } coAnswers {
            val onProgress = secondArg<(Int, Int) -> Unit>()
            onProgress(2, 2)
            ScreenshotOrganizeOutcome.LocalResults(listOf(firstResult, secondResult))
        }
        every { screenshotImageStorage.copyImageFromUri(any(), any()) } returns "/files/image"
        coEvery {
            screenshotCardRepository.saveAnalysisResults(
                results = listOf(firstResult),
                imageRefsByCaptureId = any(),
            )
        } throws RuntimeException("room failure")

        viewModel.startAnalysis(sampleImages(count = 2))
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals("Failed to save screenshot analysis result", state.errorMessage)
        assertTrue(state.results.isEmpty())
        assertFalse(state.isRunning)
        coVerify(exactly = 0) {
            screenshotCardRepository.saveAnalysisResults(
                results = listOf(secondResult),
                imageRefsByCaptureId = any(),
            )
        }

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

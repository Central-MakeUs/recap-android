package com.chalkak.recap.app

import com.chalkak.recap.core.data.screenshot.ScreenshotAnalysisInput
import com.chalkak.recap.core.data.screenshot.ScreenshotAnalysisRepository
import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import io.mockk.every
import io.mockk.mockk
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
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class ScreenshotAnalysisProgressViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<ScreenshotAnalysisRepository>()
    private lateinit var viewModel: ScreenshotAnalysisProgressViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ScreenshotAnalysisProgressViewModel(repository)
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
        every { repository.analyze(any<ScreenshotAnalysisInput>()) } returns analysisResult()

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
        every { repository.analyze(any<ScreenshotAnalysisInput>()) } returns analysisResult()

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
        every { repository.analyze(any<ScreenshotAnalysisInput>()) } returns analysisResult()

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
        every { repository.analyze(any<ScreenshotAnalysisInput>()) } returns analysisResult()

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

    private fun sampleImages(count: Int): List<LocalImage> {
        return (1..count).map { index ->
            LocalImage(
                uri = "content://$index",
                displayName = "image_$index.png",
                dateAddedMillis = index.toLong(),
            )
        }
    }

    private fun analysisResult(): ScreenshotAnalysisResult {
        return mockk(relaxed = true)
    }
}

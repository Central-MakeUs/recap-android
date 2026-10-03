package com.chalkak.recap.feature.screenshot

import app.cash.turbine.test
import com.chalkak.recap.core.data.capture.CaptureMutationRepository
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardImageRefs
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotDetailRepository
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.model.capture.CaptureDeleteResult
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScreenshotViewModelBindingTest {
    private val dispatcher = StandardTestDispatcher()
    private val details = mockk<ScreenshotDetailRepository>()
    private val mutations = mockk<CaptureMutationRepository>()
    private lateinit var viewModel: ScreenshotViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { details.observeCard(any()) } answers {
            MutableStateFlow<StoredScreenshotCard?>(card(firstArg()))
        }
        viewModel = ScreenshotViewModel(details, mutations).apply { ioDispatcher = dispatcher }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `favorite failure for previous screenshot cannot change current screenshot`() = runTest(dispatcher) {
        val result = CompletableDeferred<Result<Unit>>()
        coEvery { mutations.updateFavorite(1, true) } coAnswers { result.await() }
        viewModel.bind(1)
        advanceUntilIdle()
        viewModel.onAction(ScreenshotAction.ToggleFavorite)
        advanceUntilIdle()
        viewModel.bind(2)
        advanceUntilIdle()
        val expected = viewModel.uiState.value

        result.complete(Result.failure(IllegalStateException("offline")))
        advanceUntilIdle()

        assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `save for previous screenshot cannot navigate away from current screenshot`() = runTest(dispatcher) {
        val result = CompletableDeferred<Result<Unit>>()
        coEvery { mutations.updateCapture(any(), any(), any(), any(), any()) } coAnswers {
            result.await()
        }
        viewModel.bind(1)
        advanceUntilIdle()
        viewModel.onAction(ScreenshotAction.UpdateEditTitle("edited"))
        viewModel.onAction(ScreenshotAction.SaveEdit)
        advanceUntilIdle()
        viewModel.bind(2)
        advanceUntilIdle()
        val expected = viewModel.uiState.value

        viewModel.events.test {
            result.complete(Result.success(Unit))
            advanceUntilIdle()

            assertEquals(expected, viewModel.uiState.value)
            expectNoEvents()
        }
    }

    @Test
    fun `buffered delete event is discarded when another screenshot is opened`() = runTest(dispatcher) {
        coEvery { mutations.deleteCaptures(setOf(1L)) } returns Result.success(
            CaptureDeleteResult(deletedIds = setOf(1), failedIds = emptySet()),
        )
        viewModel.bind(1)
        advanceUntilIdle()
        viewModel.onAction(ScreenshotAction.DeleteScreenshot)
        advanceUntilIdle()
        viewModel.bind(2)
        advanceUntilIdle()

        viewModel.events.test {
            advanceUntilIdle()
            expectNoEvents()
        }
    }

    @Test
    fun `returning to same capture does not revive a previous binding operation`() = runTest(dispatcher) {
        val result = CompletableDeferred<Result<Unit>>()
        coEvery { mutations.updateFavorite(1, true) } coAnswers { result.await() }
        viewModel.bind(1)
        advanceUntilIdle()
        viewModel.onAction(ScreenshotAction.ToggleFavorite)
        advanceUntilIdle()
        viewModel.bind(2)
        advanceUntilIdle()
        viewModel.bind(1)
        advanceUntilIdle()
        val expected = viewModel.uiState.value

        viewModel.events.test {
            result.complete(Result.failure(IllegalStateException("offline")))
            advanceUntilIdle()

            assertEquals(expected, viewModel.uiState.value)
            expectNoEvents()
        }
    }

    private fun card(captureId: Long) = StoredScreenshotCard(
        analysisResult = ScreenshotAnalysisResult(
            captureId = captureId,
            typeCode = ScreenshotContentType.SHOPPING,
            title = "title-$captureId",
            summary = "summary-$captureId",
            body = "body-$captureId",
            originalImageUrl = "mock://captures/$captureId",
            isFavorite = captureId == 2L,
            organizedAt = Instant.ofEpochMilli(1000),
        ),
        imageRefs = ScreenshotCardImageRefs(storedImagePath = "/images/$captureId"),
        updatedAtMillis = 2000,
    )
}

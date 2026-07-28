package com.chalkak.recap.feature.home.recent

import com.chalkak.recap.core.data.capture.CaptureMutationRepository
import com.chalkak.recap.core.data.home.RecentCapturesRepository
import com.chalkak.recap.core.model.capture.CaptureSummary
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecentOrganizedScreenshotsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val recentCapturesRepository = mockk<RecentCapturesRepository>()
    private val captureMutationRepository = mockk<CaptureMutationRepository>()
    private val capturesFlow = MutableSharedFlow<List<CaptureSummary>>(replay = 1)
    private lateinit var viewModel: RecentOrganizedScreenshotsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { recentCapturesRepository.observeRecentCaptures() } returns capturesFlow
        viewModel = RecentOrganizedScreenshotsViewModel(
            recentCapturesRepository = recentCapturesRepository,
            captureMutationRepository = captureMutationRepository,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggle favorite updates local state immediately and calls mutation`() = runTest(testDispatcher) {
        coEvery {
            captureMutationRepository.updateFavorite(captureId = 7L, isFavorite = true)
        } returns Result.success(Unit)

        capturesFlow.emit(listOf(captureSummary(captureId = 7L, isFavorite = false)))
        advanceUntilIdle()

        viewModel.onAction(RecentOrganizedScreenshotsAction.ToggleFavorite(7L))

        assertTrue(viewModel.uiState.value.items.single().isFavorite)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.items.single().isFavorite)
        coVerify(exactly = 1) {
            captureMutationRepository.updateFavorite(captureId = 7L, isFavorite = true)
        }
    }

    @Test
    fun `toggle favorite failure rolls back local state`() = runTest(testDispatcher) {
        coEvery {
            captureMutationRepository.updateFavorite(captureId = 7L, isFavorite = true)
        } returns Result.failure(IllegalStateException("network"))

        capturesFlow.emit(listOf(captureSummary(captureId = 7L, isFavorite = false)))
        advanceUntilIdle()

        viewModel.onAction(RecentOrganizedScreenshotsAction.ToggleFavorite(7L))
        assertTrue(viewModel.uiState.value.items.single().isFavorite)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.items.single().isFavorite)
    }

    private fun captureSummary(
        captureId: Long,
        isFavorite: Boolean,
    ): CaptureSummary =
        CaptureSummary(
            captureId = captureId,
            title = "제목",
            summary = "요약",
            typeCode = ScreenshotContentType.PLACE,
            thumbnailUrl = null,
            isFavorite = isFavorite,
            organizedAt = "2026-07-19T00:00:00Z",
        )
}

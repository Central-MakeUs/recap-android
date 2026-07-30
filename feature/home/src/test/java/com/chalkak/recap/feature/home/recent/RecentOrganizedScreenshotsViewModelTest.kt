package com.chalkak.recap.feature.home.recent

import com.chalkak.recap.core.data.capture.CaptureMutationRepository
import com.chalkak.recap.core.data.home.RecentCapturesRepository
import com.chalkak.recap.core.model.capture.CapturePage
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecentOrganizedScreenshotsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val recentCapturesRepository = mockk<RecentCapturesRepository>()
    private val captureMutationRepository = mockk<CaptureMutationRepository>()
    private val firstPageFlow = MutableSharedFlow<Result<CapturePage>>(replay = 1)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every {
            recentCapturesRepository.observeRecentCaptures(page = 0)
        } returns firstPageFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads first page`() = runTest(testDispatcher) {
        firstPageFlow.tryEmit(
            Result.success(
                CapturePage(
                    count = 2,
                    hasNext = true,
                    items = listOf(
                        captureSummary(captureId = 1L, isFavorite = false),
                        captureSummary(captureId = 2L, isFavorite = false),
                    ),
                ),
            ),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(RecentOrganizedScreenshotsPhase.Content, state.phase)
        assertEquals(listOf(1L, 2L), state.items.map { it.id })
        assertEquals(2L, state.resultCount)
        assertTrue(state.hasNext)
        assertEquals(1, state.nextPage)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun `capture change refresh removes deleted item and resets pagination`() = runTest(testDispatcher) {
        firstPageFlow.tryEmit(
            Result.success(
                CapturePage(
                    count = 2,
                    hasNext = true,
                    items = listOf(
                        captureSummary(captureId = 1L, isFavorite = false),
                        captureSummary(captureId = 2L, isFavorite = false),
                    ),
                ),
            ),
        )
        coEvery {
            recentCapturesRepository.getRecentCaptures(page = 1)
        } returns Result.success(
            CapturePage(
                count = 2,
                hasNext = false,
                items = listOf(captureSummary(captureId = 3L, isFavorite = false)),
            ),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(RecentOrganizedScreenshotsAction.LoadMore)
        advanceUntilIdle()
        assertEquals(listOf(1L, 2L, 3L), viewModel.uiState.value.items.map { it.id })
        assertEquals(2, viewModel.uiState.value.nextPage)

        firstPageFlow.emit(
            Result.success(
                CapturePage(
                    count = 1,
                    hasNext = false,
                    items = listOf(captureSummary(captureId = 2L, isFavorite = false)),
                ),
            ),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(RecentOrganizedScreenshotsPhase.Content, state.phase)
        assertEquals(listOf(2L), state.items.map { it.id })
        assertEquals(1L, state.resultCount)
        assertFalse(state.hasNext)
        assertEquals(1, state.nextPage)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun `load more appends next page items`() = runTest(testDispatcher) {
        firstPageFlow.tryEmit(
            Result.success(
                CapturePage(
                    count = 3,
                    hasNext = true,
                    items = listOf(
                        captureSummary(captureId = 1L, isFavorite = false),
                        captureSummary(captureId = 2L, isFavorite = false),
                    ),
                ),
            ),
        )
        coEvery {
            recentCapturesRepository.getRecentCaptures(page = 1)
        } returns Result.success(
            CapturePage(
                count = 3,
                hasNext = false,
                items = listOf(captureSummary(captureId = 3L, isFavorite = false)),
            ),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(RecentOrganizedScreenshotsAction.LoadMore)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(1L, 2L, 3L), state.items.map { it.id })
        assertEquals(3L, state.resultCount)
        assertFalse(state.hasNext)
        assertEquals(2, state.nextPage)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun `load more is ignored when hasNext is false`() = runTest(testDispatcher) {
        firstPageFlow.tryEmit(
            Result.success(
                CapturePage(
                    count = 1,
                    hasNext = false,
                    items = listOf(captureSummary(captureId = 1L, isFavorite = false)),
                ),
            ),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(RecentOrganizedScreenshotsAction.LoadMore)
        advanceUntilIdle()

        coVerify(exactly = 0) {
            recentCapturesRepository.getRecentCaptures(page = 1)
        }
    }

    @Test
    fun `init failure sets error phase`() = runTest(testDispatcher) {
        firstPageFlow.tryEmit(Result.failure(IllegalStateException("offline")))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(RecentOrganizedScreenshotsPhase.Error, viewModel.uiState.value.phase)
    }

    @Test
    fun `refresh failure keeps previously loaded content and pagination`() = runTest(testDispatcher) {
        firstPageFlow.tryEmit(
            Result.success(
                CapturePage(
                    count = 3,
                    hasNext = true,
                    items = listOf(
                        captureSummary(captureId = 1L, isFavorite = false),
                        captureSummary(captureId = 2L, isFavorite = false),
                    ),
                ),
            ),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        firstPageFlow.emit(Result.failure(IllegalStateException("offline")))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(RecentOrganizedScreenshotsPhase.Content, state.phase)
        assertEquals(listOf(1L, 2L), state.items.map { it.id })
        assertEquals(3L, state.resultCount)
        assertTrue(state.hasNext)
        assertEquals(1, state.nextPage)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun `toggle favorite updates local state immediately and calls mutation`() = runTest(testDispatcher) {
        firstPageFlow.tryEmit(
            Result.success(
                CapturePage(
                    count = 1,
                    hasNext = false,
                    items = listOf(captureSummary(captureId = 7L, isFavorite = false)),
                ),
            ),
        )
        coEvery {
            captureMutationRepository.updateFavorite(captureId = 7L, isFavorite = true)
        } returns Result.success(Unit)

        val viewModel = createViewModel()
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
        firstPageFlow.tryEmit(
            Result.success(
                CapturePage(
                    count = 1,
                    hasNext = false,
                    items = listOf(captureSummary(captureId = 7L, isFavorite = false)),
                ),
            ),
        )
        coEvery {
            captureMutationRepository.updateFavorite(captureId = 7L, isFavorite = true)
        } returns Result.failure(IllegalStateException("network"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(RecentOrganizedScreenshotsAction.ToggleFavorite(7L))
        assertTrue(viewModel.uiState.value.items.single().isFavorite)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.items.single().isFavorite)
    }

    private fun createViewModel(): RecentOrganizedScreenshotsViewModel =
        RecentOrganizedScreenshotsViewModel(
            recentCapturesRepository = recentCapturesRepository,
            captureMutationRepository = captureMutationRepository,
        )

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

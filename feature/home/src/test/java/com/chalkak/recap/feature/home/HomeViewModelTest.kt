package com.chalkak.recap.feature.home

import com.chalkak.recap.core.data.capture.CaptureMutationRepository
import com.chalkak.recap.core.data.capture.CaptureThumbnailUpdates
import com.chalkak.recap.core.data.home.HomeRepository
import com.chalkak.recap.core.data.network.MainContentRecoveryTrigger
import com.chalkak.recap.core.model.home.HomeSummary
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val homeRepository = mockk<HomeRepository>()
    private val captureMutationRepository = mockk<CaptureMutationRepository>()
    private val thumbnailUpdates = mockk<CaptureThumbnailUpdates>()
    private val recoveryFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val mainContentRecoveryTrigger = mockk<MainContentRecoveryTrigger>()
    private val summaryResults = MutableSharedFlow<Result<HomeSummary>>(extraBufferCapacity = 1)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { homeRepository.observeSummary() } returns summaryResults
        every { homeRepository.refreshSummary() } just Runs
        every { thumbnailUpdates.thumbnailReady } returns MutableSharedFlow()
        every { thumbnailUpdates.resolveLocalPath(any()) } returns null
        every { mainContentRecoveryTrigger.recoveries } returns recoveryFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `recovery signal refreshes summary only while Error`() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(
            homeRepository = homeRepository,
            captureMutationRepository = captureMutationRepository,
            thumbnailUpdates = thumbnailUpdates,
            mainContentRecoveryTrigger = mainContentRecoveryTrigger,
        )
        runCurrent()

        summaryResults.emit(Result.failure(IllegalStateException("offline")))
        advanceUntilIdle()
        assertEquals(HomeContentPhase.Error, viewModel.uiState.value.phase)

        recoveryFlow.emit(Unit)
        advanceUntilIdle()
        verify(exactly = 1) { homeRepository.refreshSummary() }

        summaryResults.emit(
            Result.success(
                HomeSummary(
                    recentCaptures = emptyList(),
                    favorites = emptyList(),
                    topTypes = emptyList(),
                    hasAnyCapture = false,
                ),
            ),
        )
        advanceUntilIdle()
        assertEquals(HomeContentPhase.Content, viewModel.uiState.value.phase)

        recoveryFlow.emit(Unit)
        advanceUntilIdle()
        verify(exactly = 1) { homeRepository.refreshSummary() }
    }

    @Test
    fun `RetryLoad refreshes summary`() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(
            homeRepository = homeRepository,
            captureMutationRepository = captureMutationRepository,
            thumbnailUpdates = thumbnailUpdates,
            mainContentRecoveryTrigger = mainContentRecoveryTrigger,
        )
        runCurrent()

        summaryResults.emit(Result.failure(IllegalStateException("offline")))
        advanceUntilIdle()

        viewModel.onAction(HomeAction.RetryLoad)
        advanceUntilIdle()

        verify(exactly = 1) { homeRepository.refreshSummary() }
    }
}

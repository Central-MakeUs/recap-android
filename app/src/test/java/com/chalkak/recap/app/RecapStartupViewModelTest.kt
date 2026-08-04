package com.chalkak.recap.app

import app.cash.turbine.test
import com.chalkak.recap.core.data.StartupDataRecoveryCoordinator
import com.chalkak.recap.core.data.UserPreferencesRepository
import com.chalkak.recap.core.data.home.HomeRepository
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.storage.StorageRepository
import com.chalkak.recap.core.model.home.HomeSummary
import com.chalkak.recap.core.model.storage.StorageOverview
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
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
class RecapStartupViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val userPreferencesRepository = mockk<UserPreferencesRepository>()
    private val sessionTokenStore = mockk<SessionTokenStore>(relaxed = true)
    private val homeRepository = mockk<HomeRepository>()
    private val storageRepository = mockk<StorageRepository>()
    private val startupDataRecoveryCoordinator = mockk<StartupDataRecoveryCoordinator>()
    private val onboardingCompleted = MutableStateFlow(false)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { userPreferencesRepository.onboardingCompleted } returns onboardingCompleted
        coEvery { startupDataRecoveryCoordinator.recoverIfNeeded() } returns Unit
        coEvery { userPreferencesRepository.setOnboardingCompleted(any()) } coAnswers {
            onboardingCompleted.value = firstArg()
        }
        coEvery { homeRepository.prefetchSummary() } returns Result.success(
            HomeSummary(
                recentCaptures = emptyList(),
                favorites = emptyList(),
                topTypes = emptyList(),
                hasAnyCapture = false,
            ),
        )
        coEvery { storageRepository.prefetchOverview() } returns Result.success(
            StorageOverview(
                hasAnyCapture = false,
                favoriteCount = 0,
                types = emptyList(),
            ),
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loading becomes ready without prefetch when onboarding incomplete`() = runTest(testDispatcher) {
        onboardingCompleted.value = false
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(RecapStartupUiState.Loading, awaitItem())
            assertEquals(
                RecapStartupUiState.Ready(onboardingCompleted = false),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
        advanceUntilIdle()

        coVerify(exactly = 1) { startupDataRecoveryCoordinator.recoverIfNeeded() }
        coVerify(exactly = 0) { homeRepository.prefetchSummary() }
        coVerify(exactly = 0) { storageRepository.prefetchOverview() }
    }

    @Test
    fun `prefetches home and collection when onboarding is completed`() = runTest(testDispatcher) {
        onboardingCompleted.value = true
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(RecapStartupUiState.Loading, awaitItem())
            assertEquals(
                RecapStartupUiState.Ready(onboardingCompleted = true),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
        advanceUntilIdle()

        coVerify(exactly = 1) { startupDataRecoveryCoordinator.recoverIfNeeded() }
        coVerify(exactly = 1) { homeRepository.prefetchSummary() }
        coVerify(exactly = 1) { storageRepository.prefetchOverview() }
    }

    @Test
    fun `persistent IOException becomes ReadError without prefetch`() = runTest(testDispatcher) {
        coEvery {
            startupDataRecoveryCoordinator.recoverIfNeeded()
        } throws IOException("disk failed")
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(RecapStartupUiState.Loading, awaitItem())
            assertEquals(RecapStartupUiState.ReadError, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        advanceUntilIdle()

        coVerify(exactly = 0) { homeRepository.prefetchSummary() }
        coVerify(exactly = 0) { storageRepository.prefetchOverview() }
    }

    @Test
    fun `retryStartup recovers from ReadError to Ready`() = runTest(testDispatcher) {
        coEvery {
            startupDataRecoveryCoordinator.recoverIfNeeded()
        } throws IOException("disk failed") andThen Unit
        onboardingCompleted.value = true
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(RecapStartupUiState.Loading, awaitItem())
            assertEquals(RecapStartupUiState.ReadError, awaitItem())

            viewModel.retryStartup()
            assertEquals(RecapStartupUiState.Loading, awaitItem())
            assertEquals(
                RecapStartupUiState.Ready(onboardingCompleted = true),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
        advanceUntilIdle()

        coVerify(exactly = 2) { startupDataRecoveryCoordinator.recoverIfNeeded() }
        coVerify(exactly = 1) { homeRepository.prefetchSummary() }
        coVerify(exactly = 1) { storageRepository.prefetchOverview() }
    }

    @Test
    fun `recovery failure becomes ReadError without prefetch`() = runTest(testDispatcher) {
        coEvery {
            startupDataRecoveryCoordinator.recoverIfNeeded()
        } throws IllegalStateException("reset failed")
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(RecapStartupUiState.Loading, awaitItem())
            assertEquals(RecapStartupUiState.ReadError, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        advanceUntilIdle()

        coVerify(exactly = 0) { homeRepository.prefetchSummary() }
        coVerify(exactly = 0) { storageRepository.prefetchOverview() }
    }

    @Test
    fun `completeOnboarding stores completed true`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.completeOnboarding()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            userPreferencesRepository.setOnboardingCompleted(true)
        }
        assertEquals(true, onboardingCompleted.value)
        assertEquals(
            RecapStartupUiState.Ready(onboardingCompleted = true),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `resetOnboarding clears session token and completed state`() = runTest(testDispatcher) {
        onboardingCompleted.value = true
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.resetOnboarding()
        advanceUntilIdle()

        coVerify(exactly = 1) { sessionTokenStore.clear() }
        coVerify(exactly = 1) {
            userPreferencesRepository.setOnboardingCompleted(false)
        }
        assertEquals(false, onboardingCompleted.value)
        assertEquals(
            RecapStartupUiState.Ready(onboardingCompleted = false),
            viewModel.uiState.value,
        )
    }

    private fun createViewModel(): RecapStartupViewModel =
        RecapStartupViewModel(
            userPreferencesRepository = userPreferencesRepository,
            sessionTokenStore = sessionTokenStore,
            homeRepository = homeRepository,
            storageRepository = storageRepository,
            startupDataRecoveryCoordinator = startupDataRecoveryCoordinator,
        )
}

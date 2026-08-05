package com.chalkak.recap.app

import app.cash.turbine.test
import com.chalkak.recap.core.data.StartupDataRecoveryCoordinator
import com.chalkak.recap.core.data.UserPreferencesRepository
import com.chalkak.recap.core.data.home.HomeRepository
import com.chalkak.recap.core.data.network.AuthSessionStateProvider
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.storage.StorageRepository
import com.chalkak.recap.core.model.home.HomeSummary
import com.chalkak.recap.core.model.storage.StorageOverview
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
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
    private val authSessionStateProvider = mockk<AuthSessionStateProvider>()
    private val homeRepository = mockk<HomeRepository>()
    private val storageRepository = mockk<StorageRepository>()
    private val startupDataRecoveryCoordinator = mockk<StartupDataRecoveryCoordinator>()
    private val onboardingCompleted = MutableStateFlow(false)
    private val hasSession = MutableStateFlow(false)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { userPreferencesRepository.onboardingCompleted } returns onboardingCompleted
        every { authSessionStateProvider.hasSession } returns hasSession
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
    fun `loading becomes onboarding entry without prefetch when onboarding incomplete`() =
        runTest(testDispatcher) {
            onboardingCompleted.value = false
            hasSession.value = false
            val viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(RecapStartupUiState.Loading, awaitItem())
                assertEquals(readyState(RecapEntryMode.Onboarding), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            advanceUntilIdle()

            coVerify(exactly = 1) { startupDataRecoveryCoordinator.recoverIfNeeded() }
            coVerify(exactly = 0) { homeRepository.prefetchSummary() }
            coVerify(exactly = 0) { storageRepository.prefetchOverview() }
        }

    @Test
    fun `incomplete onboarding with session still enters onboarding`() = runTest(testDispatcher) {
        onboardingCompleted.value = false
        hasSession.value = true
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(readyState(RecapEntryMode.Onboarding), viewModel.uiState.value)
        coVerify(exactly = 0) { homeRepository.prefetchSummary() }
    }

    @Test
    fun `completed onboarding with session enters main and prefetches`() = runTest(testDispatcher) {
        onboardingCompleted.value = true
        hasSession.value = true
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(RecapStartupUiState.Loading, awaitItem())
            assertEquals(readyState(RecapEntryMode.Main), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        advanceUntilIdle()

        coVerify(exactly = 1) { homeRepository.prefetchSummary() }
        coVerify(exactly = 1) { storageRepository.prefetchOverview() }
    }

    @Test
    fun `completed onboarding without session enters reauth without prefetch`() =
        runTest(testDispatcher) {
            onboardingCompleted.value = true
            hasSession.value = false
            val viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(RecapStartupUiState.Loading, awaitItem())
                assertEquals(readyState(RecapEntryMode.Reauth), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            advanceUntilIdle()

            coVerify(exactly = 0) { homeRepository.prefetchSummary() }
            coVerify(exactly = 0) { storageRepository.prefetchOverview() }
        }

    @Test
    fun `session cleared while using main switches entry to reauth`() = runTest(testDispatcher) {
        onboardingCompleted.value = true
        hasSession.value = true
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(RecapStartupUiState.Loading, awaitItem())
            assertEquals(readyState(RecapEntryMode.Main), awaitItem())

            hasSession.value = false

            assertEquals(readyState(RecapEntryMode.Reauth), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reauth login success returns to main and prefetches`() = runTest(testDispatcher) {
        onboardingCompleted.value = true
        hasSession.value = false
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(readyState(RecapEntryMode.Reauth), viewModel.uiState.value)
        coVerify(exactly = 0) { homeRepository.prefetchSummary() }

        hasSession.value = true
        advanceUntilIdle()

        assertEquals(readyState(RecapEntryMode.Main), viewModel.uiState.value)
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
        hasSession.value = true
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(RecapStartupUiState.Loading, awaitItem())
            assertEquals(RecapStartupUiState.ReadError, awaitItem())

            viewModel.retryStartup()

            assertEquals(readyState(RecapEntryMode.Main), awaitItem())
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
    fun `completeOnboarding stores completed true and enters main`() = runTest(testDispatcher) {
        hasSession.value = true
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.completeOnboarding()
        advanceUntilIdle()

        coVerify(exactly = 1) { userPreferencesRepository.setOnboardingCompleted(true) }
        assertEquals(true, onboardingCompleted.value)
        assertEquals(readyState(RecapEntryMode.Main), viewModel.uiState.value)
    }

    @Test
    fun `resetOnboarding clears completed state before session token`() = runTest(testDispatcher) {
        onboardingCompleted.value = true
        hasSession.value = true
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.resetOnboarding()
        advanceUntilIdle()

        coVerifyOrder {
            userPreferencesRepository.setOnboardingCompleted(false)
            sessionTokenStore.clear()
        }
        assertEquals(false, onboardingCompleted.value)
        assertEquals(readyState(RecapEntryMode.Onboarding), viewModel.uiState.value)
    }

    private fun readyState(entryMode: RecapEntryMode): RecapStartupUiState =
        RecapStartupUiState.Ready(entryMode = entryMode)

    private fun createViewModel(): RecapStartupViewModel =
        RecapStartupViewModel(
            userPreferencesRepository = userPreferencesRepository,
            sessionTokenStore = sessionTokenStore,
            authSessionStateProvider = authSessionStateProvider,
            homeRepository = homeRepository,
            storageRepository = storageRepository,
            startupDataRecoveryCoordinator = startupDataRecoveryCoordinator,
        )
}

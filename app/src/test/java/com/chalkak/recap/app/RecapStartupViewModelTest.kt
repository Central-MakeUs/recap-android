package com.chalkak.recap.app

import app.cash.turbine.test
import com.chalkak.recap.core.data.UserPreferencesRepository
import com.chalkak.recap.core.data.home.HomeRepository
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.model.home.HomeSummary
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
    private val onboardingCompleted = MutableStateFlow(false)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { userPreferencesRepository.onboardingCompleted } returns onboardingCompleted
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
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `prefetches home summary when onboarding is completed`() = runTest(testDispatcher) {
        onboardingCompleted.value = true
        val viewModel = RecapStartupViewModel(
            userPreferencesRepository = userPreferencesRepository,
            sessionTokenStore = sessionTokenStore,
            homeRepository = homeRepository,
        )

        viewModel.uiState.test {
            assertEquals(RecapStartupUiState.Loading, awaitItem())
            assertEquals(
                RecapStartupUiState.Ready(onboardingCompleted = true),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
        advanceUntilIdle()

        coVerify(exactly = 1) { homeRepository.prefetchSummary() }
    }

    @Test
    fun `does not prefetch home summary when onboarding is incomplete`() = runTest(testDispatcher) {
        onboardingCompleted.value = false
        val viewModel = RecapStartupViewModel(
            userPreferencesRepository = userPreferencesRepository,
            sessionTokenStore = sessionTokenStore,
            homeRepository = homeRepository,
        )

        viewModel.uiState.test {
            assertEquals(RecapStartupUiState.Loading, awaitItem())
            assertEquals(
                RecapStartupUiState.Ready(onboardingCompleted = false),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
        advanceUntilIdle()

        coVerify(exactly = 0) { homeRepository.prefetchSummary() }
    }

    @Test
    fun `completeOnboarding stores completed true`() = runTest(testDispatcher) {
        val viewModel = RecapStartupViewModel(
            userPreferencesRepository = userPreferencesRepository,
            sessionTokenStore = sessionTokenStore,
            homeRepository = homeRepository,
        )

        viewModel.completeOnboarding()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            userPreferencesRepository.setOnboardingCompleted(true)
        }
        assertEquals(true, onboardingCompleted.value)
    }

    @Test
    fun `resetOnboarding clears session token and completed state`() = runTest(testDispatcher) {
        onboardingCompleted.value = true
        val viewModel = RecapStartupViewModel(
            userPreferencesRepository = userPreferencesRepository,
            sessionTokenStore = sessionTokenStore,
            homeRepository = homeRepository,
        )

        viewModel.resetOnboarding()
        advanceUntilIdle()

        coVerify(exactly = 1) { sessionTokenStore.clear() }
        coVerify(exactly = 1) {
            userPreferencesRepository.setOnboardingCompleted(false)
        }
        assertEquals(false, onboardingCompleted.value)
    }
}

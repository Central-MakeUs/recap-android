package com.chalkak.recap.feature.settings

import com.chalkak.recap.core.data.user.UserRepository
import com.chalkak.recap.core.model.user.ConsentStatus
import com.chalkak.recap.core.model.user.DataSummary
import com.chalkak.recap.core.model.user.Usage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val userRepository = mockk<UserRepository>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { userRepository.prefetchDataSummary() } returns Result.success(DataSummary(12))
        coEvery { userRepository.prefetchConsentStatus() } returns Result.success(
            ConsentStatus(consented = true),
        )
        coEvery { userRepository.prefetchUsage() } returns Result.success(
            Usage(usedCount = 0, limit = 20, remaining = 20, resetAt = ""),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun prefetchDataManagement_requestsSummaryAndConsent() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(userRepository = userRepository)

        viewModel.prefetchDataManagement()
        advanceUntilIdle()

        coVerify(exactly = 1) { userRepository.prefetchDataSummary() }
        coVerify(exactly = 1) { userRepository.prefetchConsentStatus() }
        coVerify(exactly = 1) { userRepository.prefetchUsage() }
    }

    @Test
    fun prefetchDataManagement_ignoresFailures() = runTest(testDispatcher) {
        coEvery { userRepository.prefetchDataSummary() } returns
            Result.failure(RuntimeException("offline"))
        coEvery { userRepository.prefetchConsentStatus() } returns
            Result.failure(RuntimeException("offline"))
        coEvery { userRepository.prefetchUsage() } returns
                Result.failure(RuntimeException("offline"))
        val viewModel = SettingsViewModel(userRepository = userRepository)

        viewModel.prefetchDataManagement()
        advanceUntilIdle()

        coVerify(exactly = 1) { userRepository.prefetchDataSummary() }
        coVerify(exactly = 1) { userRepository.prefetchConsentStatus() }
        coVerify(exactly = 1) { userRepository.prefetchUsage() }
    }
}

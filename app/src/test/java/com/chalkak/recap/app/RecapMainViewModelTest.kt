package com.chalkak.recap.app

import app.cash.turbine.test
import com.chalkak.recap.core.data.user.UserRepository
import com.chalkak.recap.core.model.user.Usage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecapMainViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<UserRepository>()
    private val exhausted = Usage(20, 20, 0, "2026-12-01T00:00:00Z")

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `exhausted cached usage shows sheet and confirm dismisses it`() = runTest(dispatcher) {
        coEvery { repository.getCachedUsage() } returns exhausted
        val viewModel = RecapMainViewModel(repository)
        viewModel.events.test {
            viewModel.requestOrganize()
            runCurrent()
            assertEquals(exhausted, viewModel.uiState.value.exhaustedUsage)
            expectNoEvents()
        }
        viewModel.dismissMonthlyLimit()
        assertNull(viewModel.uiState.value.exhaustedUsage)
        coVerify(exactly = 0) { repository.getUsage() }
        coVerify(exactly = 0) { repository.fetchLatestUsage() }
    }

    @Test
    fun `remaining cached usage opens organize without a sheet`() = runTest(dispatcher) {
        coEvery { repository.getCachedUsage() } returns exhausted.copy(
            usedCount = 19,
            remaining = 1
        )
        val viewModel = RecapMainViewModel(repository)
        viewModel.events.test {
            viewModel.requestOrganize()
            assertEquals(RecapMainEvent.OpenOrganize, awaitItem())
            assertNull(viewModel.uiState.value.exhaustedUsage)
        }
    }

    @Test
    fun `missing cache opens organize without fetching usage`() = runTest(dispatcher) {
        coEvery { repository.getCachedUsage() } returns null
        val viewModel = RecapMainViewModel(repository)
        viewModel.events.test {
            viewModel.requestOrganize()
            assertEquals(RecapMainEvent.OpenOrganize, awaitItem())
        }
        coVerify(exactly = 0) { repository.getUsage() }
        coVerify(exactly = 0) { repository.fetchLatestUsage() }
    }

    @Test
    fun `next click checks updated cache after dismissal`() = runTest(dispatcher) {
        coEvery { repository.getCachedUsage() } returnsMany listOf(
            exhausted,
            exhausted.copy(remaining = 20),
        )
        val viewModel = RecapMainViewModel(repository)
        viewModel.requestOrganize()
        runCurrent()
        viewModel.dismissMonthlyLimit()
        viewModel.events.test {
            viewModel.requestOrganize()
            assertEquals(RecapMainEvent.OpenOrganize, awaitItem())
        }
        assertNull(viewModel.uiState.value.exhaustedUsage)
    }
}

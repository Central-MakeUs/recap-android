package com.chalkak.recap.feature.developer

import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.screenshot.backend.MockScreenshotDataResetter
import com.chalkak.recap.core.design.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalCoroutinesApi::class)
class DeveloperViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val resetter = mockk<MockScreenshotDataResetter>()
    private val sessionTokenStore = mockk<SessionTokenStore>()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { resetter.reset() } returns Unit
        coEvery { sessionTokenStore.clear() } just runs
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invalidate session clears session token store`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(DeveloperOptionAction.InvalidateSession)
        advanceUntilIdle()

        coVerify(exactly = 1) { sessionTokenStore.clear() }
        assertEquals(
            R.string.developer_options_invalidate_session_success,
            viewModel.uiState.value.feedbackMessageResId,
        )
    }

    @Test
    fun `invalidate session failure shows failure feedback`() = runTest(testDispatcher) {
        coEvery { sessionTokenStore.clear() } throws RuntimeException("clear failed")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(DeveloperOptionAction.InvalidateSession)
        advanceUntilIdle()

        assertEquals(
            R.string.developer_options_invalidate_session_failure,
            viewModel.uiState.value.feedbackMessageResId,
        )
    }

    @Test
    fun `reset screenshot data uses mock resetter`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(DeveloperOptionAction.ResetScreenshotData)
        advanceUntilIdle()

        coVerify(exactly = 1) { resetter.reset() }
        assertEquals(
            R.string.developer_options_reset_screenshot_data_success,
            viewModel.uiState.value.feedbackMessageResId,
        )
    }

    @Test
    fun `reset screenshot data failure shows failure feedback`() = runTest(testDispatcher) {
        coEvery { resetter.reset() } throws RuntimeException("reset failed")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(DeveloperOptionAction.ResetScreenshotData)
        advanceUntilIdle()

        assertEquals(
            R.string.developer_options_reset_screenshot_data_failure,
            viewModel.uiState.value.feedbackMessageResId,
        )
    }

    @Test
    fun `force test crash throws runtime exception`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val exception = assertThrows<RuntimeException> {
            viewModel.onAction(DeveloperOptionAction.ForceTestCrash)
        }

        assertEquals("Test Crash", exception.message)
    }

    private fun createViewModel(): DeveloperViewModel {
        return DeveloperViewModel(
            mockScreenshotDataResetter = resetter,
            sessionTokenStore = sessionTokenStore,
        )
    }
}

package com.chalkak.recap.feature.developer

import com.chalkak.recap.core.data.screenshot.MockScreenshotDataResetter
import com.chalkak.recap.core.data.screenshot.ScreenshotAnalysisRunState
import com.chalkak.recap.core.data.screenshot.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.ScreenshotBackendModeStore
import com.chalkak.recap.core.data.screenshot.ScreenshotBackendSwitchResult
import com.chalkak.recap.core.data.screenshot.ScreenshotBackendSwitcher
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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeveloperViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val modeFlow = MutableStateFlow(ScreenshotBackendMode.MOCK)
    private val isSwitchingFlow = MutableStateFlow(false)
    private val modeStore = mockk<ScreenshotBackendModeStore>()
    private val switcher = mockk<ScreenshotBackendSwitcher>()
    private val resetter = mockk<MockScreenshotDataResetter>()
    private val screenshotAnalysisRunState = ScreenshotAnalysisRunState()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { modeStore.mode } returns modeFlow
        every { switcher.isSwitching } returns isSwitchingFlow
        coEvery { switcher.switchTo(any()) } coAnswers {
            modeFlow.value = firstArg()
            ScreenshotBackendSwitchResult.Success
        }
        coEvery { resetter.reset() } returns Unit
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `same mode switch request is no-op`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(
            DeveloperOptionAction.RequestScreenshotBackendSwitch(ScreenshotBackendMode.MOCK),
        )
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.pendingSwitchTargetMode)
        coVerify(exactly = 0) { switcher.switchTo(any()) }
    }

    @Test
    fun `idle switch request shows confirm dialog`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(
            DeveloperOptionAction.RequestScreenshotBackendSwitch(ScreenshotBackendMode.REMOTE),
        )
        advanceUntilIdle()

        assertEquals(
            ScreenshotBackendMode.REMOTE,
            viewModel.uiState.value.pendingSwitchTargetMode,
        )
        coVerify(exactly = 0) { switcher.switchTo(any()) }
    }

    @Test
    fun `dismiss confirm dialog keeps mode and data`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onAction(
            DeveloperOptionAction.RequestScreenshotBackendSwitch(ScreenshotBackendMode.REMOTE),
        )
        advanceUntilIdle()

        viewModel.onAction(DeveloperOptionAction.DismissScreenshotBackendSwitchDialog)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.pendingSwitchTargetMode)
        assertEquals(ScreenshotBackendMode.MOCK, viewModel.uiState.value.screenshotBackendMode)
        coVerify(exactly = 0) { switcher.switchTo(any()) }
        coVerify(exactly = 0) { resetter.reset() }
    }

    @Test
    fun `confirm switch delegates to switcher and shows success feedback`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onAction(
            DeveloperOptionAction.RequestScreenshotBackendSwitch(ScreenshotBackendMode.REMOTE),
        )
        advanceUntilIdle()

        viewModel.onAction(DeveloperOptionAction.ConfirmScreenshotBackendSwitch)
        advanceUntilIdle()

        assertEquals(ScreenshotBackendMode.REMOTE, viewModel.uiState.value.screenshotBackendMode)
        assertNull(viewModel.uiState.value.pendingSwitchTargetMode)
        assertEquals(
            com.chalkak.recap.core.design.R.string.developer_options_switch_screenshot_backend_success,
            viewModel.uiState.value.feedbackMessageResId,
        )
        coVerify(exactly = 1) { switcher.switchTo(ScreenshotBackendMode.REMOTE) }
    }

    @Test
    fun `failed switch keeps previous mode and shows failure feedback`() = runTest(testDispatcher) {
        coEvery { switcher.switchTo(any()) } returns ScreenshotBackendSwitchResult.Failure
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onAction(
            DeveloperOptionAction.RequestScreenshotBackendSwitch(ScreenshotBackendMode.REMOTE),
        )
        advanceUntilIdle()

        viewModel.onAction(DeveloperOptionAction.ConfirmScreenshotBackendSwitch)
        advanceUntilIdle()

        assertEquals(ScreenshotBackendMode.MOCK, viewModel.uiState.value.screenshotBackendMode)
        assertEquals(
            com.chalkak.recap.core.design.R.string.developer_options_switch_screenshot_backend_failure,
            viewModel.uiState.value.feedbackMessageResId,
        )
    }

    @Test
    fun `switch request while analysis running is rejected`() = runTest(testDispatcher) {
        screenshotAnalysisRunState.beginRun()
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(
            DeveloperOptionAction.RequestScreenshotBackendSwitch(ScreenshotBackendMode.REMOTE),
        )
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.pendingSwitchTargetMode)
        assertTrue(viewModel.uiState.value.isAnalysisRunning)
        assertFalse(viewModel.uiState.value.canSwitchScreenshotBackend)
        assertEquals(
            com.chalkak.recap.core.design.R.string.developer_options_switch_screenshot_backend_rejected_busy,
            viewModel.uiState.value.feedbackMessageResId,
        )
        coVerify(exactly = 0) { switcher.switchTo(any()) }
    }

    @Test
    fun `confirm while analysis running is rejected without switch`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onAction(
            DeveloperOptionAction.RequestScreenshotBackendSwitch(ScreenshotBackendMode.REMOTE),
        )
        advanceUntilIdle()
        screenshotAnalysisRunState.beginRun()

        viewModel.onAction(DeveloperOptionAction.ConfirmScreenshotBackendSwitch)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.pendingSwitchTargetMode)
        assertEquals(ScreenshotBackendMode.MOCK, viewModel.uiState.value.screenshotBackendMode)
        assertEquals(
            com.chalkak.recap.core.design.R.string.developer_options_switch_screenshot_backend_rejected_busy,
            viewModel.uiState.value.feedbackMessageResId,
        )
        coVerify(exactly = 0) { switcher.switchTo(any()) }
    }

    @Test
    fun `reset screenshot data uses mock resetter`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(DeveloperOptionAction.ResetScreenshotData)
        advanceUntilIdle()

        coVerify(exactly = 1) { resetter.reset() }
        assertEquals(
            com.chalkak.recap.core.design.R.string.developer_options_reset_screenshot_data_success,
            viewModel.uiState.value.feedbackMessageResId,
        )
    }

    private fun createViewModel(): DeveloperViewModel {
        return DeveloperViewModel(
            screenshotBackendModeStore = modeStore,
            screenshotBackendSwitcher = switcher,
            mockScreenshotDataResetter = resetter,
            screenshotAnalysisRunState = screenshotAnalysisRunState,
        )
    }
}

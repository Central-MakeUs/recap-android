package com.chalkak.recap.feature.developer

import com.chalkak.recap.core.data.UserPreferencesRepository
import com.chalkak.recap.core.data.screenshot.AnalysisDataSourceMode
import com.chalkak.recap.core.data.screenshot.ScreenshotAnalysisRunState
import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
    private val screenshotCardRepository = mockk<ScreenshotCardRepository>()
    private val screenshotImageStorage = mockk<ScreenshotImageStorage>(relaxed = true)
    private val modeFlow = MutableStateFlow(AnalysisDataSourceMode.MOCK)
    private val userPreferencesRepository = mockk<UserPreferencesRepository>()
    private val screenshotAnalysisRunState = ScreenshotAnalysisRunState()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { userPreferencesRepository.analysisDataSourceMode } returns modeFlow
        coEvery { userPreferencesRepository.setAnalysisDataSourceMode(any()) } coAnswers {
            modeFlow.value = firstArg()
        }
        coEvery { screenshotCardRepository.deleteAllCards() } returns Unit
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
            DeveloperOptionAction.RequestAnalysisDataSourceSwitch(AnalysisDataSourceMode.MOCK),
        )
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.pendingSwitchTargetMode)
        coVerify(exactly = 0) { screenshotCardRepository.deleteAllCards() }
    }

    @Test
    fun `idle switch request shows confirm dialog`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(
            DeveloperOptionAction.RequestAnalysisDataSourceSwitch(AnalysisDataSourceMode.REMOTE),
        )
        advanceUntilIdle()

        assertEquals(
            AnalysisDataSourceMode.REMOTE,
            viewModel.uiState.value.pendingSwitchTargetMode,
        )
        coVerify(exactly = 0) { screenshotCardRepository.deleteAllCards() }
    }

    @Test
    fun `dismiss confirm dialog keeps mode and data`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onAction(
            DeveloperOptionAction.RequestAnalysisDataSourceSwitch(AnalysisDataSourceMode.REMOTE),
        )
        advanceUntilIdle()

        viewModel.onAction(DeveloperOptionAction.DismissAnalysisDataSourceSwitchDialog)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.pendingSwitchTargetMode)
        assertEquals(AnalysisDataSourceMode.MOCK, viewModel.uiState.value.analysisDataSourceMode)
        coVerify(exactly = 0) { screenshotCardRepository.deleteAllCards() }
        verify(exactly = 0) { screenshotImageStorage.clearStoredImages() }
        coVerify(exactly = 0) { userPreferencesRepository.setAnalysisDataSourceMode(any()) }
    }

    @Test
    fun `confirm switch clears data then saves new mode`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onAction(
            DeveloperOptionAction.RequestAnalysisDataSourceSwitch(AnalysisDataSourceMode.REMOTE),
        )
        advanceUntilIdle()

        viewModel.onAction(DeveloperOptionAction.ConfirmAnalysisDataSourceSwitch)
        advanceUntilIdle()

        assertEquals(AnalysisDataSourceMode.REMOTE, viewModel.uiState.value.analysisDataSourceMode)
        assertNull(viewModel.uiState.value.pendingSwitchTargetMode)
        assertEquals(
            com.chalkak.recap.core.design.R.string.developer_options_switch_analysis_data_source_success,
            viewModel.uiState.value.feedbackMessageResId,
        )
        coVerify(exactly = 1) { screenshotCardRepository.deleteAllCards() }
        verify(exactly = 1) { screenshotImageStorage.clearStoredImages() }
        coVerify(exactly = 1) {
            userPreferencesRepository.setAnalysisDataSourceMode(AnalysisDataSourceMode.REMOTE)
        }
    }

    @Test
    fun `failed switch keeps previous mode and shows failure feedback`() = runTest(testDispatcher) {
        coEvery { screenshotCardRepository.deleteAllCards() } throws RuntimeException("db fail")
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onAction(
            DeveloperOptionAction.RequestAnalysisDataSourceSwitch(AnalysisDataSourceMode.REMOTE),
        )
        advanceUntilIdle()

        viewModel.onAction(DeveloperOptionAction.ConfirmAnalysisDataSourceSwitch)
        advanceUntilIdle()

        assertEquals(AnalysisDataSourceMode.MOCK, viewModel.uiState.value.analysisDataSourceMode)
        assertEquals(
            com.chalkak.recap.core.design.R.string.developer_options_switch_analysis_data_source_failure,
            viewModel.uiState.value.feedbackMessageResId,
        )
        coVerify(exactly = 0) { userPreferencesRepository.setAnalysisDataSourceMode(any()) }
    }

    @Test
    fun `switch request while analysis running is rejected`() = runTest(testDispatcher) {
        screenshotAnalysisRunState.beginRun()
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(
            DeveloperOptionAction.RequestAnalysisDataSourceSwitch(AnalysisDataSourceMode.REMOTE),
        )
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.pendingSwitchTargetMode)
        assertTrue(viewModel.uiState.value.isAnalysisRunning)
        assertFalse(viewModel.uiState.value.canSwitchAnalysisDataSource)
        assertEquals(
            com.chalkak.recap.core.design.R.string.developer_options_switch_analysis_data_source_rejected_busy,
            viewModel.uiState.value.feedbackMessageResId,
        )
        coVerify(exactly = 0) { screenshotCardRepository.deleteAllCards() }
    }

    @Test
    fun `confirm while analysis running is rejected without data changes`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onAction(
            DeveloperOptionAction.RequestAnalysisDataSourceSwitch(AnalysisDataSourceMode.REMOTE),
        )
        advanceUntilIdle()
        screenshotAnalysisRunState.beginRun()

        viewModel.onAction(DeveloperOptionAction.ConfirmAnalysisDataSourceSwitch)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.pendingSwitchTargetMode)
        assertEquals(AnalysisDataSourceMode.MOCK, viewModel.uiState.value.analysisDataSourceMode)
        assertEquals(
            com.chalkak.recap.core.design.R.string.developer_options_switch_analysis_data_source_rejected_busy,
            viewModel.uiState.value.feedbackMessageResId,
        )
        coVerify(exactly = 0) { screenshotCardRepository.deleteAllCards() }
        verify(exactly = 0) { screenshotImageStorage.clearStoredImages() }
        coVerify(exactly = 0) { userPreferencesRepository.setAnalysisDataSourceMode(any()) }
    }

    private fun createViewModel(): DeveloperViewModel {
        return DeveloperViewModel(
            screenshotCardRepository = screenshotCardRepository,
            screenshotImageStorage = screenshotImageStorage,
            userPreferencesRepository = userPreferencesRepository,
            screenshotAnalysisRunState = screenshotAnalysisRunState,
        )
    }
}

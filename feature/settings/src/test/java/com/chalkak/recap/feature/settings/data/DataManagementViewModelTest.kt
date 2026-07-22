package com.chalkak.recap.feature.settings.data

import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
class DataManagementViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val screenshotCardRepository = mockk<ScreenshotCardRepository>()
    private val screenshotImageStorage = mockk<ScreenshotImageStorage>(relaxed = true)
    private val storedCards = MutableStateFlow<List<StoredScreenshotCard>>(emptyList())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { screenshotCardRepository.observeStoredCards() } returns storedCards
        coEvery { screenshotCardRepository.deleteAllCards() } coAnswers {
            storedCards.value = emptyList()
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun deleteDataClick_showsConfirmDialog() = runTest(testDispatcher) {
        val viewModel = DataManagementViewModel(
            screenshotCardRepository,
            screenshotImageStorage,
        )

        viewModel.onAction(DataManagementAction.DeleteDataClick)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showDeleteConfirmDialog)
        coVerify(exactly = 0) { screenshotCardRepository.deleteAllCards() }
        verify(exactly = 0) { screenshotImageStorage.clearStoredImages() }
    }

    @Test
    fun dismissDeleteConfirmDialog_hidesDialog() = runTest(testDispatcher) {
        val viewModel = DataManagementViewModel(
            screenshotCardRepository,
            screenshotImageStorage,
        )
        viewModel.onAction(DataManagementAction.DeleteDataClick)
        advanceUntilIdle()

        viewModel.onAction(DataManagementAction.DismissDeleteConfirmDialog)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showDeleteConfirmDialog)
    }

    @Test
    fun confirmDeleteData_deletesAllCardsAndShowsSuccessToast() = runTest(testDispatcher) {
        storedCards.value = listOf(mockk(), mockk(), mockk())
        val viewModel = DataManagementViewModel(
            screenshotCardRepository,
            screenshotImageStorage,
        )
        advanceUntilIdle()
        viewModel.onAction(DataManagementAction.DeleteDataClick)
        advanceUntilIdle()

        val eventDeferred = async { viewModel.events.first() }
        viewModel.onAction(DataManagementAction.ConfirmDeleteData)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showDeleteConfirmDialog)
        assertEquals(0, viewModel.uiState.value.organizedCount)
        assertEquals(
            DataManagementEvent.ShowDeleteSuccessToast(deletedCount = 3),
            eventDeferred.await(),
        )
        coVerify(exactly = 1) { screenshotCardRepository.deleteAllCards() }
        verify(exactly = 1) { screenshotImageStorage.clearStoredImages() }
    }
}

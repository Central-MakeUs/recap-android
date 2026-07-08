package com.chalkak.recap.feature.cleanup

import com.chalkak.recap.core.data.LocalScreenshotDataSource
import com.chalkak.recap.core.model.LocalImage
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class CleanupViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val screenshots = List(22) { index ->
        LocalImage(
            uri = "content://screenshot/$index",
            displayName = "screenshot-$index",
            dateAddedMillis = index.toLong(),
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadScreenshots_populatesAvailableScreenshots() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        coEvery { dataSource.queryAllScreenshots() } returns screenshots.take(3)
        val viewModel = CleanupViewModel(dataSource)

        advanceUntilIdle()

        assertEquals(
            CleanupUiState(
                isLoading = false,
                availableScreenshots = screenshots.take(3),
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun toggleSelection_addsAndRemovesInSelectionOrder() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        coEvery { dataSource.queryAllScreenshots() } returns screenshots.take(3)
        val viewModel = CleanupViewModel(dataSource)
        advanceUntilIdle()

        viewModel.onAction(CleanupAction.ToggleSelection(screenshots[0].uri))
        viewModel.onAction(CleanupAction.ToggleSelection(screenshots[1].uri))
        viewModel.onAction(CleanupAction.ToggleSelection(screenshots[2].uri))

        assertEquals(
            listOf(
                screenshots[0].uri,
                screenshots[1].uri,
                screenshots[2].uri,
            ),
            viewModel.uiState.value.selectedUris,
        )
        assertEquals(1, viewModel.uiState.value.selectionOrder(screenshots[0].uri))
        assertEquals(2, viewModel.uiState.value.selectionOrder(screenshots[1].uri))
        assertEquals(3, viewModel.uiState.value.selectionOrder(screenshots[2].uri))

        viewModel.onAction(CleanupAction.ToggleSelection(screenshots[1].uri))

        assertEquals(
            listOf(
                screenshots[0].uri,
                screenshots[2].uri,
            ),
            viewModel.uiState.value.selectedUris,
        )
        assertEquals(1, viewModel.uiState.value.selectionOrder(screenshots[0].uri))
        assertEquals(2, viewModel.uiState.value.selectionOrder(screenshots[2].uri))
    }

    @Test
    fun toggleSelection_enforcesMaxSelectionCount() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        coEvery { dataSource.queryAllScreenshots() } returns screenshots
        val viewModel = CleanupViewModel(dataSource)
        advanceUntilIdle()

        screenshots.take(MAX_SELECTION_COUNT).forEach { screenshot ->
            viewModel.onAction(CleanupAction.ToggleSelection(screenshot.uri))
        }
        viewModel.onAction(CleanupAction.ToggleSelection(screenshots[MAX_SELECTION_COUNT].uri))

        assertEquals(MAX_SELECTION_COUNT, viewModel.uiState.value.selectionCount)
        assertTrue(viewModel.uiState.value.showMaxSelectionReached)
    }

    @Test
    fun removeSelection_reordersRemainingSelections() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        coEvery { dataSource.queryAllScreenshots() } returns screenshots.take(3)
        val viewModel = CleanupViewModel(dataSource)
        advanceUntilIdle()

        viewModel.onAction(CleanupAction.ToggleSelection(screenshots[0].uri))
        viewModel.onAction(CleanupAction.ToggleSelection(screenshots[1].uri))
        viewModel.onAction(CleanupAction.ToggleSelection(screenshots[2].uri))
        viewModel.onAction(CleanupAction.RemoveSelection(screenshots[1].uri))

        assertEquals(
            listOf(
                screenshots[0].uri,
                screenshots[2].uri,
            ),
            viewModel.uiState.value.selectedUris,
        )
        assertEquals(1, viewModel.uiState.value.selectionOrder(screenshots[0].uri))
        assertEquals(2, viewModel.uiState.value.selectionOrder(screenshots[2].uri))
    }

    @Test
    fun canProceed_requiresAtLeastOneSelection() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        coEvery { dataSource.queryAllScreenshots() } returns screenshots.take(1)
        val viewModel = CleanupViewModel(dataSource)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canProceed)

        viewModel.onAction(CleanupAction.ToggleSelection(screenshots[0].uri))

        assertTrue(viewModel.uiState.value.canProceed)
    }
}

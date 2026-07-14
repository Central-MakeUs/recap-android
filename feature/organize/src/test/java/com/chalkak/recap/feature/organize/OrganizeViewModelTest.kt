package com.chalkak.recap.feature.organize

import com.chalkak.recap.core.data.LocalScreenshotDataSource
import com.chalkak.recap.core.model.LocalImage
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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
class OrganizeViewModelTest {
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
        val viewModel = createViewModel(dataSource)

        assertEquals(
            OrganizeUiState(
                isLoading = false,
                availableScreenshots = screenshots.take(3),
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun refreshScreenshots_updatesAvailableScreenshots() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        coEvery { dataSource.queryAllScreenshots() } returnsMany listOf(
            screenshots.take(2),
            screenshots.take(4),
        )
        val viewModel = OrganizeViewModel(dataSource)

        viewModel.refreshScreenshots()
        advanceUntilIdle()

        assertEquals(screenshots.take(2), viewModel.uiState.value.availableScreenshots)

        viewModel.refreshScreenshots()
        advanceUntilIdle()

        assertEquals(screenshots.take(4), viewModel.uiState.value.availableScreenshots)
    }

    @Test
    fun toggleSelection_addsAndRemovesInSelectionOrder() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        coEvery { dataSource.queryAllScreenshots() } returns screenshots.take(3)
        val viewModel = createViewModel(dataSource)

        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[0].uri))
        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[1].uri))
        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[2].uri))

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

        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[1].uri))

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
        val viewModel = createViewModel(dataSource)

        screenshots.take(MAX_SELECTION_COUNT).forEach { screenshot ->
            viewModel.onAction(OrganizeAction.ToggleSelection(screenshot.uri))
        }
        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[MAX_SELECTION_COUNT].uri))

        assertEquals(MAX_SELECTION_COUNT, viewModel.uiState.value.selectionCount)
        assertTrue(viewModel.uiState.value.showMaxSelectionReached)
    }

    @Test
    fun removeSelection_reordersRemainingSelections() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        coEvery { dataSource.queryAllScreenshots() } returns screenshots.take(3)
        val viewModel = createViewModel(dataSource)

        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[0].uri))
        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[1].uri))
        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[2].uri))
        viewModel.onAction(OrganizeAction.RemoveSelection(screenshots[1].uri))

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
        val viewModel = createViewModel(dataSource)

        assertFalse(viewModel.uiState.value.canProceed)

        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[0].uri))

        assertTrue(viewModel.uiState.value.canProceed)
    }

    @Test
    fun clearSelection_resetsSelectedUrisAndMaxSelectionFlag() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        coEvery { dataSource.queryAllScreenshots() } returns screenshots
        val viewModel = createViewModel(dataSource)

        screenshots.take(MAX_SELECTION_COUNT).forEach { screenshot ->
            viewModel.onAction(OrganizeAction.ToggleSelection(screenshot.uri))
        }
        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[MAX_SELECTION_COUNT].uri))
        viewModel.onAction(OrganizeAction.ClearSelection)

        assertEquals(emptyList<String>(), viewModel.uiState.value.selectedUris)
        assertFalse(viewModel.uiState.value.showMaxSelectionReached)
        assertFalse(viewModel.uiState.value.canProceed)
    }

    private fun TestScope.createViewModel(dataSource: LocalScreenshotDataSource): OrganizeViewModel {
        val viewModel = OrganizeViewModel(dataSource)
        viewModel.refreshScreenshots()
        advanceUntilIdle()
        return viewModel
    }
}

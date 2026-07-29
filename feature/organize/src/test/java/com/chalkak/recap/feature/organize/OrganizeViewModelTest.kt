package com.chalkak.recap.feature.organize

import androidx.lifecycle.SavedStateHandle
import com.chalkak.recap.core.data.LocalScreenshotDataSource
import com.chalkak.recap.core.data.user.UserRepository
import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.core.model.user.ConsentStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrganizeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val userRepository = mockk<UserRepository>()
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
        coEvery { userRepository.getConsentStatus() } returns Result.success(
            ConsentStatus(consented = true),
        )
        coEvery { userRepository.giveConsent() } returns Result.success(Unit)
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
        val viewModel = OrganizeViewModel(dataSource, userRepository, SavedStateHandle())

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

    @Test
    fun `refresh after clear keeps empty selection for in-app reentry`() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        coEvery { dataSource.queryAllScreenshots() } returns screenshots
        val viewModel = createViewModel(dataSource)

        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[0].uri))
        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[1].uri))
        viewModel.onAction(OrganizeAction.ClearSelection)
        viewModel.refreshScreenshots()
        advanceUntilIdle()

        assertEquals(emptyList<String>(), viewModel.uiState.value.selectedUris)
        assertEquals(screenshots, viewModel.uiState.value.availableScreenshots)
        assertFalse(viewModel.uiState.value.canProceed)
    }

    @Test
    fun `refresh without clear preserves previous selection`() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        coEvery { dataSource.queryAllScreenshots() } returns screenshots
        val viewModel = createViewModel(dataSource)

        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[0].uri))
        viewModel.refreshScreenshots()
        advanceUntilIdle()

        assertEquals(listOf(screenshots[0].uri), viewModel.uiState.value.selectedUris)
    }

    @Test
    fun seedSharedImages_setsAvailableAndSelected() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        val shared = screenshots.take(3)
        val viewModel = OrganizeViewModel(dataSource, userRepository, SavedStateHandle())

        viewModel.seedSharedImages(
            sessionId = "share-session",
            images = shared,
        )

        assertEquals(shared, viewModel.uiState.value.availableScreenshots)
        assertEquals(shared.map { it.uri }, viewModel.uiState.value.selectedUris)
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.canProceed)
    }

    @Test
    fun seedSharedImages_sameSessionDoesNotResetCurrentSelection() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        val shared = screenshots.take(3)
        val viewModel = OrganizeViewModel(dataSource, userRepository, SavedStateHandle())

        viewModel.seedSharedImages(
            sessionId = "share-session",
            images = shared,
        )
        viewModel.onAction(OrganizeAction.RemoveSelection(shared[1].uri))
        viewModel.seedSharedImages(
            sessionId = "share-session",
            images = shared,
        )

        assertEquals(
            listOf(shared[0].uri, shared[2].uri),
            viewModel.uiState.value.selectedUris,
        )
    }

    @Test
    fun seedSharedImages_newSessionReplacesPreviousSelection() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        val firstShare = screenshots.take(2)
        val secondShare = screenshots.drop(2).take(2)
        val viewModel = OrganizeViewModel(dataSource, userRepository, SavedStateHandle())

        viewModel.seedSharedImages(
            sessionId = "first-session",
            images = firstShare,
        )
        viewModel.onAction(OrganizeAction.RemoveSelection(firstShare[0].uri))
        viewModel.seedSharedImages(
            sessionId = "second-session",
            images = secondShare,
        )

        assertEquals(secondShare, viewModel.uiState.value.availableScreenshots)
        assertEquals(
            secondShare.map { image -> image.uri },
            viewModel.uiState.value.selectedUris,
        )
    }

    @Test
    fun refreshScreenshotsMergingSelected_keepsSharedOrphans() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        val gallery = screenshots.take(2)
        val sharedOnly = LocalImage(
            uri = "content://share/orphan",
            displayName = "orphan.jpg",
            dateAddedMillis = 99L,
        )
        coEvery { dataSource.queryAllScreenshots() } returns gallery
        val viewModel = OrganizeViewModel(dataSource, userRepository, SavedStateHandle())
        viewModel.seedSharedImages(
            sessionId = "share-session",
            images = listOf(sharedOnly) + gallery.take(1),
        )

        viewModel.refreshScreenshotsMergingSelected()
        advanceUntilIdle()

        assertEquals(
            listOf(sharedOnly) + gallery,
            viewModel.uiState.value.availableScreenshots,
        )
        assertEquals(
            listOf(sharedOnly.uri, gallery[0].uri),
            viewModel.uiState.value.selectedUris,
        )
    }

    @Test
    fun `restored share session keeps edited selection`() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        val savedStateHandle = SavedStateHandle()
        val shared = screenshots.take(3)
        val viewModel = OrganizeViewModel(dataSource, userRepository, savedStateHandle)
        viewModel.seedSharedImages(
            sessionId = "share-session",
            images = shared,
        )
        viewModel.onAction(OrganizeAction.RemoveSelection(shared[1].uri))

        val restoredViewModel = OrganizeViewModel(
            dataSource,
            userRepository,
            savedStateHandle,
        )
        restoredViewModel.seedSharedImages(
            sessionId = "share-session",
            images = shared,
        )

        assertEquals(shared, restoredViewModel.uiState.value.availableScreenshots)
        assertEquals(
            listOf(shared[0].uri, shared[2].uri),
            restoredViewModel.uiState.value.selectedUris,
        )
    }

    @Test
    fun `stale gallery refresh does not overwrite newer share session`() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        val queryStarted = CompletableDeferred<Unit>()
        val queryResult = CompletableDeferred<List<LocalImage>>()
        coEvery { dataSource.queryAllScreenshots() } coAnswers {
            queryStarted.complete(Unit)
            withContext(NonCancellable) {
                queryResult.await()
            }
        }
        val viewModel = OrganizeViewModel(dataSource, userRepository, SavedStateHandle())
        val shared = screenshots.drop(10).take(2)

        viewModel.refreshScreenshots()
        runCurrent()
        queryStarted.await()
        viewModel.seedSharedImages(
            sessionId = "new-share-session",
            images = shared,
        )
        queryResult.complete(screenshots.take(3))
        advanceUntilIdle()

        assertEquals(shared, viewModel.uiState.value.availableScreenshots)
        assertEquals(
            shared.map { image -> image.uri },
            viewModel.uiState.value.selectedUris,
        )
    }

    @Test
    fun `startOrganizing with consented status emits proceed event`() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        coEvery { dataSource.queryAllScreenshots() } returns screenshots.take(1)
        coEvery { userRepository.getConsentStatus() } returns Result.success(
            ConsentStatus(consented = true),
        )
        val viewModel = createViewModel(dataSource)
        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[0].uri))
        viewModel.onConfirmationEntered()
        advanceUntilIdle()

        val events = mutableListOf<OrganizeEvent>()
        val collectJob = launch { viewModel.events.collect { events.add(it) } }
        runCurrent()

        viewModel.onAction(OrganizeAction.StartOrganizing)
        advanceUntilIdle()

        assertEquals(listOf(OrganizeEvent.ProceedToOrganize), events)
        assertFalse(viewModel.uiState.value.showAiDataTransferConsentSheet)
        collectJob.cancel()
    }

    @Test
    fun `startOrganizing waits for pending consent fetch before proceeding`() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        coEvery { dataSource.queryAllScreenshots() } returns screenshots.take(1)
        val consentStarted = CompletableDeferred<Unit>()
        val consentResult = CompletableDeferred<ConsentStatus>()
        coEvery { userRepository.getConsentStatus() } coAnswers {
            consentStarted.complete(Unit)
            withContext(NonCancellable) {
                Result.success(consentResult.await())
            }
        }
        val viewModel = createViewModel(dataSource)
        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[0].uri))
        viewModel.onConfirmationEntered()
        runCurrent()
        consentStarted.await()

        val events = mutableListOf<OrganizeEvent>()
        val collectJob = launch { viewModel.events.collect { events.add(it) } }
        runCurrent()

        viewModel.onAction(OrganizeAction.StartOrganizing)
        runCurrent()
        assertTrue(events.isEmpty())

        consentResult.complete(ConsentStatus(consented = true))
        advanceUntilIdle()

        assertEquals(listOf(OrganizeEvent.ProceedToOrganize), events)
        collectJob.cancel()
    }

    @Test
    fun `startOrganizing without consent shows consent sheet`() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        coEvery { dataSource.queryAllScreenshots() } returns screenshots.take(1)
        coEvery { userRepository.getConsentStatus() } returns Result.success(
            ConsentStatus(consented = false),
        )
        val viewModel = createViewModel(dataSource)
        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[0].uri))
        viewModel.onConfirmationEntered()
        advanceUntilIdle()

        viewModel.onAction(OrganizeAction.StartOrganizing)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showAiDataTransferConsentSheet)
    }

    @Test
    fun `startOrganizing after consent fetch failure shows consent sheet`() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        coEvery { dataSource.queryAllScreenshots() } returns screenshots.take(1)
        coEvery { userRepository.getConsentStatus() } returns Result.failure(IllegalStateException())
        val viewModel = createViewModel(dataSource)
        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[0].uri))
        viewModel.onConfirmationEntered()
        advanceUntilIdle()

        viewModel.onAction(OrganizeAction.StartOrganizing)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showAiDataTransferConsentSheet)
    }

    @Test
    fun `agree consent success emits proceed and hides sheet`() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        coEvery { dataSource.queryAllScreenshots() } returns screenshots.take(1)
        coEvery { userRepository.getConsentStatus() } returns Result.success(
            ConsentStatus(consented = false),
        )
        coEvery { userRepository.giveConsent() } returns Result.success(Unit)
        val viewModel = createViewModel(dataSource)
        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[0].uri))
        viewModel.onConfirmationEntered()
        advanceUntilIdle()
        viewModel.onAction(OrganizeAction.StartOrganizing)
        advanceUntilIdle()

        val events = mutableListOf<OrganizeEvent>()
        val collectJob = launch { viewModel.events.collect { events.add(it) } }
        runCurrent()

        viewModel.onAction(OrganizeAction.AgreeAiDataTransferConsent)
        advanceUntilIdle()

        assertEquals(listOf(OrganizeEvent.ProceedToOrganize), events)
        assertFalse(viewModel.uiState.value.showAiDataTransferConsentSheet)
        assertFalse(viewModel.uiState.value.isConsentSubmitting)
        coVerify(exactly = 1) { userRepository.giveConsent() }
        collectJob.cancel()
    }

    @Test
    fun `agree consent failure keeps sheet open`() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        coEvery { dataSource.queryAllScreenshots() } returns screenshots.take(1)
        coEvery { userRepository.getConsentStatus() } returns Result.success(
            ConsentStatus(consented = false),
        )
        coEvery { userRepository.giveConsent() } returns Result.failure(IllegalStateException())
        val viewModel = createViewModel(dataSource)
        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[0].uri))
        viewModel.onConfirmationEntered()
        advanceUntilIdle()
        viewModel.onAction(OrganizeAction.StartOrganizing)
        advanceUntilIdle()

        viewModel.onAction(OrganizeAction.AgreeAiDataTransferConsent)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showAiDataTransferConsentSheet)
        assertFalse(viewModel.uiState.value.isConsentSubmitting)
    }

    @Test
    fun `dismiss consent sheet hides sheet without proceeding`() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        coEvery { dataSource.queryAllScreenshots() } returns screenshots.take(1)
        coEvery { userRepository.getConsentStatus() } returns Result.success(
            ConsentStatus(consented = false),
        )
        val viewModel = createViewModel(dataSource)
        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[0].uri))
        viewModel.onConfirmationEntered()
        advanceUntilIdle()
        viewModel.onAction(OrganizeAction.StartOrganizing)
        advanceUntilIdle()

        viewModel.onAction(OrganizeAction.DismissAiDataTransferConsent)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showAiDataTransferConsentSheet)
    }

    @Test
    fun `confirmation reentry fetches fresh consent status`() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        coEvery { dataSource.queryAllScreenshots() } returns screenshots.take(1)
        coEvery { userRepository.getConsentStatus() } returnsMany listOf(
            Result.success(ConsentStatus(consented = false)),
            Result.success(ConsentStatus(consented = true)),
        )
        val viewModel = createViewModel(dataSource)
        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[0].uri))

        viewModel.onConfirmationEntered()
        advanceUntilIdle()
        viewModel.onConfirmationExited()
        viewModel.onConfirmationEntered()
        advanceUntilIdle()

        val events = mutableListOf<OrganizeEvent>()
        val collectJob = launch { viewModel.events.collect { events.add(it) } }
        runCurrent()

        viewModel.onAction(OrganizeAction.StartOrganizing)
        advanceUntilIdle()

        assertEquals(listOf(OrganizeEvent.ProceedToOrganize), events)
        coVerify(exactly = 2) { userRepository.getConsentStatus() }
        collectJob.cancel()
    }

    @Test
    fun `confirmation exit prevents pending consent result from proceeding`() = runTest {
        val dataSource = mockk<LocalScreenshotDataSource>()
        coEvery { dataSource.queryAllScreenshots() } returns screenshots.take(1)
        val consentStarted = CompletableDeferred<Unit>()
        val consentResult = CompletableDeferred<ConsentStatus>()
        coEvery { userRepository.getConsentStatus() } coAnswers {
            consentStarted.complete(Unit)
            withContext(NonCancellable) {
                Result.success(consentResult.await())
            }
        }
        val viewModel = createViewModel(dataSource)
        viewModel.onAction(OrganizeAction.ToggleSelection(screenshots[0].uri))
        val events = mutableListOf<OrganizeEvent>()
        val collectJob = launch { viewModel.events.collect { events.add(it) } }
        runCurrent()

        viewModel.onConfirmationEntered()
        runCurrent()
        consentStarted.await()
        viewModel.onAction(OrganizeAction.StartOrganizing)
        runCurrent()
        viewModel.onConfirmationExited()
        consentResult.complete(ConsentStatus(consented = true))
        advanceUntilIdle()

        assertTrue(events.isEmpty())
        assertFalse(viewModel.uiState.value.showAiDataTransferConsentSheet)
        collectJob.cancel()
    }

    private fun TestScope.createViewModel(dataSource: LocalScreenshotDataSource): OrganizeViewModel {
        val viewModel = OrganizeViewModel(dataSource, userRepository, SavedStateHandle())
        viewModel.refreshScreenshots()
        advanceUntilIdle()
        return viewModel
    }
}

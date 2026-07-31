package com.chalkak.recap.feature.settings.data

import com.chalkak.recap.core.data.user.UserRepository
import com.chalkak.recap.core.model.user.ConsentStatus
import com.chalkak.recap.core.model.user.DataSummary
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DataManagementViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val userRepository = mockk<UserRepository>()
    private val dataSummary = MutableStateFlow(Result.success(DataSummary(0)))
    private val consentStatus = MutableStateFlow(Result.success(ConsentStatus(consented = false)))

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { userRepository.observeDataSummary() } returns dataSummary
        every { userRepository.refreshDataSummary() } just runs
        every { userRepository.observeConsentStatus() } returns consentStatus
        every { userRepository.refreshConsentStatus() } just runs
        dataSummary.value = Result.success(DataSummary(0))
        consentStatus.value = Result.success(ConsentStatus(consented = false))
        coEvery { userRepository.deleteAccountData() } returns Result.success(Unit)
        coEvery { userRepository.giveConsent() } returns Result.success(Unit)
        coEvery { userRepository.withdrawConsent() } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        DataManagementViewModel(
            userRepository = userRepository,
        )

    @Test
    fun initialState_hasNullOrganizedCountAndConsent() = runTest(testDispatcher) {
        every { userRepository.observeDataSummary() } returns flowOf()
        every { userRepository.observeConsentStatus() } returns flowOf()
        val viewModel = createViewModel()

        assertNull(viewModel.uiState.value.organizedCount)
        assertNull(viewModel.uiState.value.isAiDataTransferConsented)
    }

    @Test
    fun loadDataSummary_updatesOrganizedCount() = runTest(testDispatcher) {
        every { userRepository.observeDataSummary() } returns flowOf(
            Result.success(DataSummary(128)),
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(128, viewModel.uiState.value.organizedCount)
    }

    @Test
    fun loadConsentStatus_updatesConsentUi() = runTest(testDispatcher) {
        consentStatus.value = Result.success(
            ConsentStatus(
                consented = true,
                consentedAt = "2026-07-27T00:00:00Z",
            ),
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.isAiDataTransferConsented)
        assertEquals("2026.07.27", viewModel.uiState.value.aiDataTransferConsentDate)
    }

    @Test
    fun fetchFailure_setsErrorAndBlocksActions() = runTest(testDispatcher) {
        every { userRepository.observeDataSummary() } returns flowOf(
            Result.failure(RuntimeException("offline")),
        )
        every { userRepository.observeConsentStatus() } returns flowOf(
            Result.failure(RuntimeException("offline")),
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasFetchError)
        assertNull(viewModel.uiState.value.organizedCount)
        assertNull(viewModel.uiState.value.isAiDataTransferConsented)

        viewModel.onAction(DataManagementAction.DeleteDataClick)
        viewModel.onAction(DataManagementAction.AiDataTransferConsentClick)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showDeleteConfirmDialog)
        assertFalse(viewModel.uiState.value.showAiDataTransferConsentSheet)
        assertFalse(viewModel.uiState.value.showWithdrawConsentDialog)
        coVerify(exactly = 0) { userRepository.deleteAccountData() }
    }

    @Test
    fun fetchFailure_clearsAfterAllSourcesRecover() = runTest(testDispatcher) {
        dataSummary.value = Result.failure(RuntimeException("offline"))
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.hasFetchError)

        dataSummary.value = Result.success(DataSummary(3))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasFetchError)
        assertEquals(3, viewModel.uiState.value.organizedCount)

        viewModel.onAction(DataManagementAction.DeleteDataClick)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showDeleteConfirmDialog)
    }

    @Test
    fun fetchFailure_blocksActionsEvenWhenPartialDataLoaded() = runTest(testDispatcher) {
        every { userRepository.observeDataSummary() } returns flowOf(
            Result.success(DataSummary(3)),
        )
        every { userRepository.observeConsentStatus() } returns flowOf(
            Result.failure(RuntimeException("offline")),
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasFetchError)
        assertEquals(3, viewModel.uiState.value.organizedCount)

        viewModel.onAction(DataManagementAction.DeleteDataClick)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showDeleteConfirmDialog)
        coVerify(exactly = 0) { userRepository.deleteAccountData() }
    }

    @Test
    fun fetchFailure_allowsOpenDialogToBeDismissed() = runTest(testDispatcher) {
        dataSummary.value = Result.success(DataSummary(3))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onAction(DataManagementAction.DeleteDataClick)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showDeleteConfirmDialog)

        dataSummary.value = Result.failure(RuntimeException("offline"))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.hasFetchError)

        viewModel.onAction(DataManagementAction.DismissDeleteConfirmDialog)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showDeleteConfirmDialog)
    }

    @Test
    fun deleteDataClick_isNoOpWhileCountLoading() = runTest(testDispatcher) {
        every { userRepository.observeDataSummary() } returns flowOf()
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.organizedCount)

        viewModel.onAction(DataManagementAction.DeleteDataClick)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showDeleteConfirmDialog)
        coVerify(exactly = 0) { userRepository.deleteAccountData() }
    }

    @Test
    fun deleteDataClick_showsEmptyToastWhenCountIsZero() = runTest(testDispatcher) {
        every { userRepository.observeDataSummary() } returns flowOf(
            Result.success(DataSummary(0)),
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        val eventDeferred = async { viewModel.events.first() }
        viewModel.onAction(DataManagementAction.DeleteDataClick)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showDeleteConfirmDialog)
        assertEquals(DataManagementEvent.ShowNoDataToDeleteToast, eventDeferred.await())
        coVerify(exactly = 0) { userRepository.deleteAccountData() }
    }

    @Test
    fun deleteDataClick_showsConfirmDialog() = runTest(testDispatcher) {
        every { userRepository.observeDataSummary() } returns flowOf(
            Result.success(DataSummary(3)),
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(DataManagementAction.DeleteDataClick)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showDeleteConfirmDialog)
        coVerify(exactly = 0) { userRepository.deleteAccountData() }
    }

    @Test
    fun dismissDeleteConfirmDialog_hidesDialog() = runTest(testDispatcher) {
        every { userRepository.observeDataSummary() } returns flowOf(
            Result.success(DataSummary(3)),
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onAction(DataManagementAction.DeleteDataClick)
        advanceUntilIdle()

        viewModel.onAction(DataManagementAction.DismissDeleteConfirmDialog)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showDeleteConfirmDialog)
    }

    @Test
    fun confirmDeleteData_deletesThenShowsSuccessToast() = runTest(testDispatcher) {
        every { userRepository.observeDataSummary() } returns flowOf(
            Result.success(DataSummary(3)),
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.organizedCount)
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
        coVerify(exactly = 1) { userRepository.deleteAccountData() }
        verify(exactly = 0) { userRepository.refreshDataSummary() }
    }

    @Test
    fun confirmDeleteData_keepsCountWhenDeleteFails() = runTest(testDispatcher) {
        every { userRepository.observeDataSummary() } returns flowOf(
            Result.success(DataSummary(5)),
        )
        coEvery { userRepository.deleteAccountData() } returns
            Result.failure(RuntimeException("offline"))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onAction(DataManagementAction.DeleteDataClick)
        advanceUntilIdle()

        viewModel.onAction(DataManagementAction.ConfirmDeleteData)
        advanceUntilIdle()

        assertEquals(5, viewModel.uiState.value.organizedCount)
        coVerify(exactly = 1) { userRepository.deleteAccountData() }
        verify(exactly = 0) { userRepository.refreshDataSummary() }
    }

    @Test
    fun aiDataTransferConsentClick_showsConsentSheetWhenNotConsented() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(false, viewModel.uiState.value.isAiDataTransferConsented)

        viewModel.onAction(DataManagementAction.AiDataTransferConsentClick)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showAiDataTransferConsentSheet)
        assertEquals(false, viewModel.uiState.value.isAiDataTransferConsented)
        coVerify(exactly = 0) { userRepository.giveConsent() }
        coVerify(exactly = 0) { userRepository.withdrawConsent() }
    }

    @Test
    fun aiDataTransferConsentClick_isNoOpWhileLoading() = runTest(testDispatcher) {
        every { userRepository.observeConsentStatus() } returns flowOf()
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.isAiDataTransferConsented)

        viewModel.onAction(DataManagementAction.AiDataTransferConsentClick)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showAiDataTransferConsentSheet)
        assertFalse(viewModel.uiState.value.showWithdrawConsentDialog)
    }

    @Test
    fun agreeAiDataTransferConsent_givesConsentAndKeepsSheetForUiHide() = runTest(testDispatcher) {
        coEvery { userRepository.giveConsent() } answers {
            consentStatus.value = Result.success(
                ConsentStatus(
                    consented = true,
                    consentedAt = "2026-07-27T12:00:00Z",
                ),
            )
            Result.success(Unit)
        }
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onAction(DataManagementAction.AiDataTransferConsentClick)
        advanceUntilIdle()

        viewModel.onAction(DataManagementAction.AgreeAiDataTransferConsent)
        advanceUntilIdle()

        // hide 애니메이션은 UI의 sheetState.hide()가 담당하고, 완료 후 Dismiss로 닫힌다.
        assertTrue(viewModel.uiState.value.showAiDataTransferConsentSheet)
        assertEquals(true, viewModel.uiState.value.isAiDataTransferConsented)
        assertEquals("2026.07.27", viewModel.uiState.value.aiDataTransferConsentDate)
        coVerify(exactly = 1) { userRepository.giveConsent() }
        coVerify(exactly = 0) { userRepository.withdrawConsent() }
        verify(exactly = 0) { userRepository.refreshConsentStatus() }

        viewModel.onAction(DataManagementAction.DismissAiDataTransferConsent)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showAiDataTransferConsentSheet)
    }

    @Test
    fun agreeAiDataTransferConsent_keepsSheetOpenWhenRemoteFails() = runTest(testDispatcher) {
        coEvery { userRepository.giveConsent() } returns
            Result.failure(RuntimeException("offline"))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onAction(DataManagementAction.AiDataTransferConsentClick)
        advanceUntilIdle()

        viewModel.onAction(DataManagementAction.AgreeAiDataTransferConsent)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showAiDataTransferConsentSheet)
        assertEquals(false, viewModel.uiState.value.isAiDataTransferConsented)
        coVerify(exactly = 1) { userRepository.giveConsent() }
        verify(exactly = 0) { userRepository.refreshConsentStatus() }
    }

    @Test
    fun dismissAiDataTransferConsent_hidesSheetWithoutGivingConsent() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onAction(DataManagementAction.AiDataTransferConsentClick)
        advanceUntilIdle()

        viewModel.onAction(DataManagementAction.DismissAiDataTransferConsent)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showAiDataTransferConsentSheet)
        assertEquals(false, viewModel.uiState.value.isAiDataTransferConsented)
        coVerify(exactly = 0) { userRepository.giveConsent() }
    }

    @Test
    fun aiDataTransferConsentClick_showsWithdrawConfirmDialogWhenConsented() = runTest(testDispatcher) {
        consentStatus.value = Result.success(
            ConsentStatus(
                consented = true,
                consentedAt = "2026-07-27T00:00:00Z",
            ),
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(DataManagementAction.AiDataTransferConsentClick)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showWithdrawConsentDialog)
        coVerify(exactly = 0) { userRepository.withdrawConsent() }
    }

    @Test
    fun dismissWithdrawConsentDialog_hidesDialog() = runTest(testDispatcher) {
        consentStatus.value = Result.success(
            ConsentStatus(
                consented = true,
                consentedAt = "2026-07-27T00:00:00Z",
            ),
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onAction(DataManagementAction.AiDataTransferConsentClick)
        advanceUntilIdle()

        viewModel.onAction(DataManagementAction.DismissWithdrawConsentDialog)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showWithdrawConsentDialog)
        coVerify(exactly = 0) { userRepository.withdrawConsent() }
    }

    @Test
    fun confirmWithdrawConsent_withdrawsAndObservesUpdatedStatus() = runTest(testDispatcher) {
        consentStatus.value = Result.success(
            ConsentStatus(
                consented = true,
                consentedAt = "2026-07-27T00:00:00Z",
            ),
        )
        coEvery { userRepository.withdrawConsent() } answers {
            consentStatus.value = Result.success(ConsentStatus(consented = false))
            Result.success(Unit)
        }
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onAction(DataManagementAction.AiDataTransferConsentClick)
        advanceUntilIdle()

        val eventDeferred = async { viewModel.events.first() }
        viewModel.onAction(DataManagementAction.ConfirmWithdrawConsent)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showWithdrawConsentDialog)
        assertEquals(false, viewModel.uiState.value.isAiDataTransferConsented)
        assertEquals("", viewModel.uiState.value.aiDataTransferConsentDate)
        assertEquals(DataManagementEvent.ShowConsentWithdrawnToast, eventDeferred.await())
        coVerify(exactly = 1) { userRepository.withdrawConsent() }
        verify(exactly = 0) { userRepository.refreshConsentStatus() }
    }

    @Test
    fun confirmWithdrawConsent_keepsStateWhenRemoteFails() = runTest(testDispatcher) {
        consentStatus.value = Result.success(
            ConsentStatus(
                consented = true,
                consentedAt = "2026-07-27T00:00:00Z",
            ),
        )
        coEvery { userRepository.withdrawConsent() } returns
            Result.failure(RuntimeException("offline"))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onAction(DataManagementAction.AiDataTransferConsentClick)
        advanceUntilIdle()

        viewModel.onAction(DataManagementAction.ConfirmWithdrawConsent)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showWithdrawConsentDialog)
        assertEquals(true, viewModel.uiState.value.isAiDataTransferConsented)
        assertEquals("2026.07.27", viewModel.uiState.value.aiDataTransferConsentDate)
        coVerify(exactly = 1) { userRepository.withdrawConsent() }
        verify(exactly = 0) { userRepository.refreshConsentStatus() }
    }
}

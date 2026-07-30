package com.chalkak.recap.feature.settings.data

import com.chalkak.recap.core.data.user.UserRepository
import com.chalkak.recap.core.model.user.ConsentStatus
import com.chalkak.recap.core.model.user.DataSummary
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
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
    private val userRepository = mockk<UserRepository>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { userRepository.getDataSummary() } returns Result.success(DataSummary(0))
        coEvery { userRepository.getConsentStatus() } returns Result.success(
            ConsentStatus(consented = false),
        )
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
    fun loadDataSummary_updatesOrganizedCount() = runTest(testDispatcher) {
        coEvery { userRepository.getDataSummary() } returns Result.success(DataSummary(128))
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(128, viewModel.uiState.value.organizedCount)
    }

    @Test
    fun loadConsentStatus_updatesConsentUi() = runTest(testDispatcher) {
        coEvery { userRepository.getConsentStatus() } returns Result.success(
            ConsentStatus(
                consented = true,
                consentedAt = "2026-07-27T00:00:00Z",
            ),
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAiDataTransferConsented)
        assertEquals("2026.07.27", viewModel.uiState.value.aiDataTransferConsentDate)
    }

    @Test
    fun deleteDataClick_showsConfirmDialog() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(DataManagementAction.DeleteDataClick)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showDeleteConfirmDialog)
        coVerify(exactly = 0) { userRepository.deleteAccountData() }
    }

    @Test
    fun dismissDeleteConfirmDialog_hidesDialog() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.onAction(DataManagementAction.DeleteDataClick)
        advanceUntilIdle()

        viewModel.onAction(DataManagementAction.DismissDeleteConfirmDialog)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showDeleteConfirmDialog)
    }

    @Test
    fun confirmDeleteData_deletesThenShowsSuccessToast() = runTest(testDispatcher) {
        coEvery { userRepository.getDataSummary() } returnsMany listOf(
            Result.success(DataSummary(3)),
            Result.success(DataSummary(0)),
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
    }

    @Test
    fun confirmDeleteData_keepsCountWhenDeleteFails() = runTest(testDispatcher) {
        coEvery { userRepository.getDataSummary() } returns Result.success(DataSummary(5))
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
    }

    @Test
    fun aiDataTransferConsentClick_showsConsentSheetWhenNotConsented() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isAiDataTransferConsented)

        viewModel.onAction(DataManagementAction.AiDataTransferConsentClick)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showAiDataTransferConsentSheet)
        assertFalse(viewModel.uiState.value.isAiDataTransferConsented)
        coVerify(exactly = 0) { userRepository.giveConsent() }
        coVerify(exactly = 0) { userRepository.withdrawConsent() }
    }

    @Test
    fun agreeAiDataTransferConsent_givesConsentAndKeepsSheetForUiHide() = runTest(testDispatcher) {
        coEvery { userRepository.getConsentStatus() } returnsMany listOf(
            Result.success(ConsentStatus(consented = false)),
            Result.success(
                ConsentStatus(
                    consented = true,
                    consentedAt = "2026-07-27T12:00:00Z",
                ),
            ),
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onAction(DataManagementAction.AiDataTransferConsentClick)
        advanceUntilIdle()

        viewModel.onAction(DataManagementAction.AgreeAiDataTransferConsent)
        advanceUntilIdle()

        // hide 애니메이션은 UI의 sheetState.hide()가 담당하고, 완료 후 Dismiss로 닫힌다.
        assertTrue(viewModel.uiState.value.showAiDataTransferConsentSheet)
        assertTrue(viewModel.uiState.value.isAiDataTransferConsented)
        assertEquals("2026.07.27", viewModel.uiState.value.aiDataTransferConsentDate)
        coVerify(exactly = 1) { userRepository.giveConsent() }
        coVerify(exactly = 0) { userRepository.withdrawConsent() }

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
        assertFalse(viewModel.uiState.value.isAiDataTransferConsented)
        coVerify(exactly = 1) { userRepository.giveConsent() }
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
        assertFalse(viewModel.uiState.value.isAiDataTransferConsented)
        coVerify(exactly = 0) { userRepository.giveConsent() }
    }

    @Test
    fun aiDataTransferConsentClick_showsWithdrawConfirmDialogWhenConsented() = runTest(testDispatcher) {
        coEvery { userRepository.getConsentStatus() } returns Result.success(
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
        coEvery { userRepository.getConsentStatus() } returns Result.success(
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
    fun confirmWithdrawConsent_withdrawsAndRefreshesStatus() = runTest(testDispatcher) {
        coEvery { userRepository.getConsentStatus() } returnsMany listOf(
            Result.success(
                ConsentStatus(
                    consented = true,
                    consentedAt = "2026-07-27T00:00:00Z",
                ),
            ),
            Result.success(ConsentStatus(consented = false)),
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onAction(DataManagementAction.AiDataTransferConsentClick)
        advanceUntilIdle()

        val eventDeferred = async { viewModel.events.first() }
        viewModel.onAction(DataManagementAction.ConfirmWithdrawConsent)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showWithdrawConsentDialog)
        assertFalse(viewModel.uiState.value.isAiDataTransferConsented)
        assertEquals("", viewModel.uiState.value.aiDataTransferConsentDate)
        assertEquals(DataManagementEvent.ShowConsentWithdrawnToast, eventDeferred.await())
        coVerify(exactly = 1) { userRepository.withdrawConsent() }
    }

    @Test
    fun confirmWithdrawConsent_keepsStateWhenRemoteFails() = runTest(testDispatcher) {
        coEvery { userRepository.getConsentStatus() } returns Result.success(
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
        assertTrue(viewModel.uiState.value.isAiDataTransferConsented)
        assertEquals("2026.07.27", viewModel.uiState.value.aiDataTransferConsentDate)
        coVerify(exactly = 1) { userRepository.withdrawConsent() }
        coVerify(exactly = 1) { userRepository.getConsentStatus() }
    }
}

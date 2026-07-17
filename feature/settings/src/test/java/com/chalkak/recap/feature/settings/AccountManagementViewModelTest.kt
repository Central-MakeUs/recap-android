package com.chalkak.recap.feature.settings

import com.chalkak.recap.core.data.LocalAppDataResetter
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountManagementViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val localAppDataResetter = mockk<LocalAppDataResetter>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { localAppDataResetter.resetDatabaseAndOnboarding() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun logoutClick_showsLogoutDialog() {
        val viewModel = AccountManagementViewModel(localAppDataResetter)

        viewModel.onAction(AccountManagementAction.LogoutClick)

        assertEquals(AccountManagementDialog.Logout, viewModel.uiState.value.dialog)
    }

    @Test
    fun withdrawClick_showsWithdrawDialog() {
        val viewModel = AccountManagementViewModel(localAppDataResetter)

        viewModel.onAction(AccountManagementAction.WithdrawClick)

        assertEquals(AccountManagementDialog.Withdraw, viewModel.uiState.value.dialog)
    }

    @Test
    fun dismissDialog_clearsDialog() {
        val viewModel = AccountManagementViewModel(localAppDataResetter)
        viewModel.onAction(AccountManagementAction.LogoutClick)

        viewModel.onAction(AccountManagementAction.DismissDialog)

        assertEquals(AccountManagementDialog.None, viewModel.uiState.value.dialog)
    }

    @Test
    fun confirmLogout_resetsLocalDataAndClearsDialog() = runTest(testDispatcher) {
        val viewModel = AccountManagementViewModel(localAppDataResetter)
        viewModel.onAction(AccountManagementAction.LogoutClick)

        viewModel.onAction(AccountManagementAction.ConfirmLogout)
        advanceUntilIdle()

        assertEquals(AccountManagementDialog.None, viewModel.uiState.value.dialog)
        coVerify(exactly = 1) { localAppDataResetter.resetDatabaseAndOnboarding() }
    }

    @Test
    fun confirmWithdraw_resetsLocalDataAndClearsDialog() = runTest(testDispatcher) {
        val viewModel = AccountManagementViewModel(localAppDataResetter)
        viewModel.onAction(AccountManagementAction.WithdrawClick)

        viewModel.onAction(AccountManagementAction.ConfirmWithdraw)
        advanceUntilIdle()

        assertEquals(AccountManagementDialog.None, viewModel.uiState.value.dialog)
        coVerify(exactly = 1) { localAppDataResetter.resetDatabaseAndOnboarding() }
    }
}

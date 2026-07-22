package com.chalkak.recap.feature.settings.account

import com.chalkak.recap.core.data.LocalAppDataResetter
import com.chalkak.recap.core.data.auth.AuthRepository
import com.chalkak.recap.core.model.auth.KakaoUserProfile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
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
    private val authRepository = mockk<AuthRepository>()
    private val localAppDataResetter = mockk<LocalAppDataResetter>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { authRepository.logout() } returns Result.success(Unit)
        coEvery { authRepository.getKakaoUserProfile() } returns Result.success(
            KakaoUserProfile(
                email = null,
                connectedAt = null,
                emailNeedsAgreement = false,
            ),
        )
        coEvery { localAppDataResetter.resetDatabaseAndOnboarding() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun logoutClick_showsLogoutDialog() {
        val viewModel = AccountManagementViewModel(authRepository, localAppDataResetter)

        viewModel.onAction(AccountManagementAction.LogoutClick)

        assertEquals(AccountManagementDialog.Logout, viewModel.uiState.value.dialog)
    }

    @Test
    fun withdrawClick_showsWithdrawDialog() {
        val viewModel = AccountManagementViewModel(authRepository, localAppDataResetter)

        viewModel.onAction(AccountManagementAction.WithdrawClick)

        assertEquals(AccountManagementDialog.Withdraw, viewModel.uiState.value.dialog)
    }

    @Test
    fun dismissDialog_clearsDialog() {
        val viewModel = AccountManagementViewModel(authRepository, localAppDataResetter)
        viewModel.onAction(AccountManagementAction.LogoutClick)

        viewModel.onAction(AccountManagementAction.DismissDialog)

        assertEquals(AccountManagementDialog.None, viewModel.uiState.value.dialog)
    }

    @Test
    fun confirmLogout_logsOutAndResetsLocalData() = runTest(testDispatcher) {
        val viewModel = AccountManagementViewModel(authRepository, localAppDataResetter)
        viewModel.onAction(AccountManagementAction.LogoutClick)

        viewModel.onAction(AccountManagementAction.ConfirmLogout)
        advanceUntilIdle()

        assertEquals(AccountManagementDialog.None, viewModel.uiState.value.dialog)
        coVerify(exactly = 1) { authRepository.logout() }
        coVerify(exactly = 1) { localAppDataResetter.resetDatabaseAndOnboarding() }
    }

    @Test
    fun confirmWithdraw_logsOutAndResetsLocalData() = runTest(testDispatcher) {
        val viewModel = AccountManagementViewModel(authRepository, localAppDataResetter)
        viewModel.onAction(AccountManagementAction.WithdrawClick)

        viewModel.onAction(AccountManagementAction.ConfirmWithdraw)
        advanceUntilIdle()

        assertEquals(AccountManagementDialog.None, viewModel.uiState.value.dialog)
        coVerify(exactly = 1) { authRepository.logout() }
        coVerify(exactly = 1) { localAppDataResetter.resetDatabaseAndOnboarding() }
    }

    @Test
    fun confirmLogout_resetsLocalDataEvenWhenServerLogoutFails() = runTest(testDispatcher) {
        coEvery { authRepository.logout() } returns Result.failure(RuntimeException("offline"))
        val viewModel = AccountManagementViewModel(authRepository, localAppDataResetter)

        viewModel.onAction(AccountManagementAction.ConfirmLogout)
        advanceUntilIdle()

        coVerify(exactly = 1) { authRepository.logout() }
        coVerify(exactly = 1) { localAppDataResetter.resetDatabaseAndOnboarding() }
    }

    @Test
    fun loadAccountInfo_formatsJoinedDate() = runTest(testDispatcher) {
        coEvery { authRepository.getKakaoUserProfile() } returns Result.success(
            KakaoUserProfile(
                email = "Recap@kakao.com",
                connectedAt = Instant.parse("2026-06-12T00:00:00Z"),
                emailNeedsAgreement = false,
            ),
        )
        val viewModel = AccountManagementViewModel(authRepository, localAppDataResetter)

        viewModel.loadAccountInfo()
        advanceUntilIdle()

        assertEquals("2026.6.12", viewModel.uiState.value.joinedDate)
    }

    @Test
    fun loadAccountInfo_leavesJoinedDateEmptyWhenProfileFails() = runTest(testDispatcher) {
        coEvery { authRepository.getKakaoUserProfile() } returns
            Result.failure(RuntimeException("offline"))
        val viewModel = AccountManagementViewModel(authRepository, localAppDataResetter)

        viewModel.loadAccountInfo()
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.joinedDate)
    }
}

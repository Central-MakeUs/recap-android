package com.chalkak.recap.feature.settings.account

import com.chalkak.recap.core.data.LocalAppDataResetter
import com.chalkak.recap.core.data.auth.AuthRepository
import com.chalkak.recap.core.data.user.UserRepository
import com.chalkak.recap.core.model.user.AccountInfo
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
    private val authRepository = mockk<AuthRepository>()
    private val userRepository = mockk<UserRepository>()
    private val localAppDataResetter = mockk<LocalAppDataResetter>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { authRepository.logout() } returns Result.success(Unit)
        coEvery { userRepository.withdraw() } returns Result.success(Unit)
        coEvery { userRepository.getAccountInfo() } returns Result.success(
            AccountInfo(
                platform = "kakao",
                createdAt = "2026-06-12T00:00:00Z",
            ),
        )
        coEvery { localAppDataResetter.resetDatabaseAndOnboarding() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        AccountManagementViewModel(authRepository, userRepository, localAppDataResetter)

    @Test
    fun logoutClick_showsLogoutDialog() {
        val viewModel = createViewModel()

        viewModel.onAction(AccountManagementAction.LogoutClick)

        assertEquals(AccountManagementDialog.Logout, viewModel.uiState.value.dialog)
    }

    @Test
    fun withdrawClick_showsWithdrawDialog() {
        val viewModel = createViewModel()

        viewModel.onAction(AccountManagementAction.WithdrawClick)

        assertEquals(AccountManagementDialog.Withdraw, viewModel.uiState.value.dialog)
    }

    @Test
    fun dismissDialog_clearsDialog() {
        val viewModel = createViewModel()
        viewModel.onAction(AccountManagementAction.LogoutClick)

        viewModel.onAction(AccountManagementAction.DismissDialog)

        assertEquals(AccountManagementDialog.None, viewModel.uiState.value.dialog)
    }

    @Test
    fun confirmLogout_logsOutAndResetsLocalData() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.onAction(AccountManagementAction.LogoutClick)

        viewModel.onAction(AccountManagementAction.ConfirmLogout)
        advanceUntilIdle()

        assertEquals(AccountManagementDialog.None, viewModel.uiState.value.dialog)
        coVerify(exactly = 1) { authRepository.logout() }
        coVerify(exactly = 0) { userRepository.withdraw() }
        coVerify(exactly = 1) { localAppDataResetter.resetDatabaseAndOnboarding() }
    }

    @Test
    fun confirmWithdraw_withdrawsAndResetsLocalData() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.onAction(AccountManagementAction.WithdrawClick)

        viewModel.onAction(AccountManagementAction.ConfirmWithdraw)
        advanceUntilIdle()

        assertEquals(AccountManagementDialog.None, viewModel.uiState.value.dialog)
        coVerify(exactly = 1) { userRepository.withdraw() }
        coVerify(exactly = 0) { authRepository.logout() }
        coVerify(exactly = 1) { localAppDataResetter.resetDatabaseAndOnboarding() }
    }

    @Test
    fun confirmLogout_resetsLocalDataEvenWhenServerLogoutFails() = runTest(testDispatcher) {
        coEvery { authRepository.logout() } returns Result.failure(RuntimeException("offline"))
        val viewModel = createViewModel()

        viewModel.onAction(AccountManagementAction.ConfirmLogout)
        advanceUntilIdle()

        coVerify(exactly = 1) { authRepository.logout() }
        coVerify(exactly = 1) { localAppDataResetter.resetDatabaseAndOnboarding() }
    }

    @Test
    fun confirmWithdraw_resetsLocalDataEvenWhenServerWithdrawFails() = runTest(testDispatcher) {
        coEvery { userRepository.withdraw() } returns Result.failure(RuntimeException("offline"))
        val viewModel = createViewModel()

        viewModel.onAction(AccountManagementAction.ConfirmWithdraw)
        advanceUntilIdle()

        coVerify(exactly = 1) { userRepository.withdraw() }
        coVerify(exactly = 1) { localAppDataResetter.resetDatabaseAndOnboarding() }
    }

    @Test
    fun loadAccountInfo_formatsJoinedDateAndPlatform() = runTest(testDispatcher) {
        coEvery { userRepository.getAccountInfo() } returns Result.success(
            AccountInfo(
                platform = "kakao",
                createdAt = "2026-06-12T00:00:00Z",
            ),
        )
        val viewModel = createViewModel()

        viewModel.loadAccountInfo()
        advanceUntilIdle()

        assertEquals(LoginPlatform.Kakao, viewModel.uiState.value.platform)
        assertEquals(
            formatJoinedDateFromIso("2026-06-12T00:00:00Z"),
            viewModel.uiState.value.joinedDate,
        )
    }

    @Test
    fun loadAccountInfo_mapsApplePlatform() = runTest(testDispatcher) {
        coEvery { userRepository.getAccountInfo() } returns Result.success(
            AccountInfo(
                platform = "apple",
                createdAt = "2026-07-01T00:00:00Z",
            ),
        )
        val viewModel = createViewModel()

        viewModel.loadAccountInfo()
        advanceUntilIdle()

        assertEquals(LoginPlatform.Apple, viewModel.uiState.value.platform)
        assertEquals(
            formatJoinedDateFromIso("2026-07-01T00:00:00Z"),
            viewModel.uiState.value.joinedDate,
        )
    }

    @Test
    fun loadAccountInfo_leavesJoinedDateEmptyWhenRequestFails() = runTest(testDispatcher) {
        coEvery { userRepository.getAccountInfo() } returns
            Result.failure(RuntimeException("offline"))
        val viewModel = createViewModel()

        viewModel.loadAccountInfo()
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.joinedDate)
        assertEquals(LoginPlatform.Kakao, viewModel.uiState.value.platform)
    }
}

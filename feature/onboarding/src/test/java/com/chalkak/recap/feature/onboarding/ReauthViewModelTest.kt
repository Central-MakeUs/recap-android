package com.chalkak.recap.feature.onboarding

import android.content.Context
import com.chalkak.recap.core.data.auth.AuthException
import com.chalkak.recap.core.data.auth.AuthRepository
import com.chalkak.recap.core.data.network.NetworkConnectivityMonitor
import com.chalkak.recap.core.model.auth.AuthError
import com.chalkak.recap.core.model.auth.AuthSignInResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReauthViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val authRepository = mockk<AuthRepository>()
    private val networkConnectivityMonitor = mockk<NetworkConnectivityMonitor>()
    private val context = mockk<Context>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { networkConnectivityMonitor.isInternetValidated() } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `successful login clears loading without emitting an error`() = runTest(testDispatcher) {
        coEvery { authRepository.signInWithKakao(context) } returns Result.success(
            AuthSignInResult.Success(
                accessToken = "access-token",
                refreshToken = "refresh-token",
                accessTokenExpiresAt = "2026-08-05T00:00:00Z",
            ),
        )
        val viewModel = ReauthViewModel(authRepository, networkConnectivityMonitor)

        viewModel.loginWithKakao(context)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        coVerify(exactly = 1) { authRepository.signInWithKakao(context) }
    }

    @Test
    fun `failed login emits login error event`() = runTest(testDispatcher) {
        coEvery { authRepository.signInWithKakao(context) } returns Result.failure(
            AuthException(AuthError.Unknown),
        )
        val viewModel = ReauthViewModel(authRepository, networkConnectivityMonitor)
        val event = async { viewModel.events.first() }
        advanceUntilIdle()

        viewModel.loginWithKakao(context)
        advanceUntilIdle()

        assertEquals(ReauthEvent.ShowLoginError(isCancelled = false), event.await())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `cancelled login marks the error as cancelled`() = runTest(testDispatcher) {
        coEvery { authRepository.signInWithKakao(context) } returns Result.failure(
            AuthException(AuthError.Cancelled),
        )
        val viewModel = ReauthViewModel(authRepository, networkConnectivityMonitor)
        val event = async { viewModel.events.first() }
        advanceUntilIdle()

        viewModel.loginWithKakao(context)
        advanceUntilIdle()

        assertEquals(ReauthEvent.ShowLoginError(isCancelled = true), event.await())
    }

    @Test
    fun `offline login emits no internet without calling auth`() = runTest(testDispatcher) {
        every { networkConnectivityMonitor.isInternetValidated() } returns false
        val viewModel = ReauthViewModel(authRepository, networkConnectivityMonitor)
        val event = async { viewModel.events.first() }
        advanceUntilIdle()

        viewModel.loginWithKakao(context)
        advanceUntilIdle()

        assertEquals(ReauthEvent.ShowNoInternet, event.await())
        assertFalse(viewModel.uiState.value.isLoading)
        coVerify(exactly = 0) { authRepository.signInWithKakao(any()) }
    }

    @Test
    fun `network failure emits no internet`() = runTest(testDispatcher) {
        coEvery { authRepository.signInWithKakao(context) } returns Result.failure(
            AuthException(AuthError.Network),
        )
        val viewModel = ReauthViewModel(authRepository, networkConnectivityMonitor)
        val event = async { viewModel.events.first() }
        advanceUntilIdle()

        viewModel.loginWithKakao(context)
        advanceUntilIdle()

        assertEquals(ReauthEvent.ShowNoInternet, event.await())
        assertFalse(viewModel.uiState.value.isLoading)
    }
}

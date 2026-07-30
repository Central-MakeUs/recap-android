package com.chalkak.recap.feature.onboarding

import android.content.Context
import com.chalkak.recap.core.data.auth.AuthException
import com.chalkak.recap.core.data.auth.AuthRepository
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.screenshot.permission.ImagePermissionRepository
import com.chalkak.recap.core.model.ImageAccessLevel
import com.chalkak.recap.core.model.auth.AuthError
import com.chalkak.recap.core.model.auth.AuthSignInResult
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onAction_movesToStartFirstAnalyzeOnSkipFirstOrganize() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(OnboardingAction.SkipFirstOrganize)
        advanceUntilIdle()

        assertEquals(OnboardingStep.StartFirstAnalyze, viewModel.uiState.value.step)
    }

    @Test
    fun onAction_completeAddToFavoriteWinsOverInitialization() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.onAction(OnboardingAction.CompleteAddToFavorite)
        advanceUntilIdle()

        assertEquals(OnboardingStep.StartFirstAnalyze, viewModel.uiState.value.step)
    }

    @Test
    fun onAction_movesBackToPreviousStep() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(OnboardingAction.SkipFirstOrganize)
        viewModel.onAction(OnboardingAction.Back)
        advanceUntilIdle()

        assertEquals(OnboardingStep.AddToFavorite, viewModel.uiState.value.step)
    }

    @Test
    fun onAction_selectStepMovesToRequestedStep() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(OnboardingAction.SelectStep(OnboardingStep.UploadMethodGuide))
        advanceUntilIdle()

        assertEquals(OnboardingStep.UploadMethodGuide, viewModel.uiState.value.step)
    }

    @Test
    fun createdViewModel_withoutSession_staysOnLanding() = runTest(testDispatcher) {
        val sessionTokenStore = mockk<SessionTokenStore>()
        coEvery { sessionTokenStore.getRefreshToken() } returns null
        val viewModel = createViewModel(
            sessionTokenStore = sessionTokenStore,
            imageAccessLevel = ImageAccessLevel.Full,
        )
        advanceUntilIdle()

        assertEquals(OnboardingStep.Landing, viewModel.uiState.value.step)
        assertEquals(ImageAccessLevel.Full, viewModel.uiState.value.imageAccessLevel)
    }

    @Test
    fun createdViewModel_withSession_startsAtPermissionGuide() = runTest(testDispatcher) {
        val sessionTokenStore = mockk<SessionTokenStore>()
        coEvery { sessionTokenStore.getRefreshToken() } returns "refresh"
        val authRepository = mockk<AuthRepository>(relaxed = true)
        coEvery { authRepository.refresh() } returns Result.success(
            AuthSignInResult.Success(
                accessToken = "access",
                refreshToken = "refresh",
                accessTokenExpiresAt = "2026-07-10T13:00:00Z",
            ),
        )

        val viewModel = createViewModel(
            authRepository = authRepository,
            sessionTokenStore = sessionTokenStore,
        )
        advanceUntilIdle()

        assertEquals(OnboardingStep.PermissionGuide, viewModel.uiState.value.step)
    }

    @Test
    fun createdViewModel_withInvalidRefresh_clearsSessionAndStaysOnLanding() = runTest(testDispatcher) {
        val sessionTokenStore = mockk<SessionTokenStore>(relaxed = true)
        coEvery { sessionTokenStore.getRefreshToken() } returns "refresh"
        val authRepository = mockk<AuthRepository>(relaxed = true)
        coEvery { authRepository.refresh() } returns Result.failure(
            AuthException(AuthError.Server(code = "INVALID_REFRESH_TOKEN", message = "expired")),
        )

        val viewModel = createViewModel(
            authRepository = authRepository,
            sessionTokenStore = sessionTokenStore,
        )
        advanceUntilIdle()

        assertEquals(OnboardingStep.Landing, viewModel.uiState.value.step)
        coVerify { sessionTokenStore.clear() }
    }

    @Test
    fun loginWithKakao_movesToPermissionGuideOnSuccess() = runTest(testDispatcher) {
        val context = mockk<Context>(relaxed = true)
        val authRepository = mockk<AuthRepository>(relaxed = true)
        coEvery { authRepository.signInWithKakao(context) } returns Result.success(
            AuthSignInResult.Success(
                accessToken = "access",
                refreshToken = "refresh",
                accessTokenExpiresAt = "2026-07-10T13:00:00Z",
            ),
        )
        val viewModel = createViewModel(authRepository = authRepository)
        advanceUntilIdle()

        viewModel.loginWithKakao(context)
        advanceUntilIdle()

        assertEquals(OnboardingStep.PermissionGuide, viewModel.uiState.value.step)
        assertFalse(viewModel.uiState.value.isLoading)
        coVerify(exactly = 1) { authRepository.signInWithKakao(context) }
    }

    @Test
    fun loginWithKakao_staysOnLandingWhenCancelled() = runTest(testDispatcher) {
        val context = mockk<Context>(relaxed = true)
        val authRepository = mockk<AuthRepository>(relaxed = true)
        coEvery { authRepository.signInWithKakao(context) } returns Result.failure(
            AuthException(AuthError.Cancelled),
        )
        val viewModel = createViewModel(authRepository = authRepository)
        advanceUntilIdle()
        val eventDeferred = async { viewModel.events.first() }

        viewModel.loginWithKakao(context)
        advanceUntilIdle()

        assertEquals(OnboardingStep.Landing, viewModel.uiState.value.step)
        assertFalse(viewModel.uiState.value.isLoading)
        val errorEvent = eventDeferred.await() as OnboardingEvent.ShowLoginError
        assertEquals(true, errorEvent.isCancelled)
    }

    @Test
    fun loginWithKakao_emitsLoginErrorOnFailure() = runTest(testDispatcher) {
        val context = mockk<Context>(relaxed = true)
        val authRepository = mockk<AuthRepository>(relaxed = true)
        coEvery { authRepository.signInWithKakao(context) } returns Result.failure(
            AuthException(AuthError.Server(code = "OAUTH_VERIFICATION_FAILED", message = "실패")),
        )
        val viewModel = createViewModel(authRepository = authRepository)
        advanceUntilIdle()
        val eventDeferred = async { viewModel.events.first() }

        viewModel.loginWithKakao(context)
        advanceUntilIdle()

        assertEquals(OnboardingStep.Landing, viewModel.uiState.value.step)
        val errorEvent = eventDeferred.await() as OnboardingEvent.ShowLoginError
        assertEquals(false, errorEvent.isCancelled)
    }

    private fun createViewModel(
        imageAccessLevel: ImageAccessLevel = ImageAccessLevel.Denied,
        authRepository: AuthRepository = mockk(relaxed = true),
        sessionTokenStore: SessionTokenStore? = null,
    ): OnboardingViewModel {
        val resolvedSessionTokenStore = sessionTokenStore ?: mockk<SessionTokenStore>(relaxed = true).also {
            coEvery { it.getRefreshToken() } returns null
        }
        return OnboardingViewModel(
            imagePermissionRepository = FakeImagePermissionRepository(imageAccessLevel),
            authRepository = authRepository,
            sessionTokenStore = resolvedSessionTokenStore,
        )
    }
}

private class FakeImagePermissionRepository(
    private val imageAccessLevel: ImageAccessLevel = ImageAccessLevel.Denied,
) : ImagePermissionRepository {
    override fun imagePermissionRequest(): Array<String> = emptyArray()

    override fun currentImageAccessLevel(): ImageAccessLevel = imageAccessLevel
}

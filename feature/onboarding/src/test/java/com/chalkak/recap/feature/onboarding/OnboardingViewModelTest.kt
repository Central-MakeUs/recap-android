package com.chalkak.recap.feature.onboarding

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.chalkak.recap.core.data.auth.AuthException
import com.chalkak.recap.core.data.auth.AuthRepository
import com.chalkak.recap.core.data.ocr.ImagePermissionRepository
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
    fun onAction_savesMovedStepToSavedStateHandle() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle = savedStateHandle)

        viewModel.onAction(OnboardingAction.SkipFirstOrganize)

        assertEquals(OnboardingStep.StartFirstAnalyze, viewModel.uiState.value.step)
        assertEquals(
            OnboardingStep.StartFirstAnalyze.name,
            savedStateHandle.get<String>(ONBOARDING_STEP_SAVED_STATE_KEY),
        )
    }

    @Test
    fun onAction_savesBackStepToSavedStateHandle() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle = savedStateHandle)

        viewModel.onAction(OnboardingAction.SkipFirstOrganize)
        viewModel.onAction(OnboardingAction.Back)

        assertEquals(OnboardingStep.AddToFavorite, viewModel.uiState.value.step)
        assertEquals(
            OnboardingStep.AddToFavorite.name,
            savedStateHandle.get<String>(ONBOARDING_STEP_SAVED_STATE_KEY),
        )
    }

    @Test
    fun createdViewModel_restoresSavedStepAndReadsCurrentPermissionLevel() {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                ONBOARDING_STEP_SAVED_STATE_KEY to OnboardingStep.AddToFavorite.name,
            ),
        )
        val viewModel = createViewModel(
            savedStateHandle = savedStateHandle,
            imageAccessLevel = ImageAccessLevel.Full,
        )

        assertEquals(OnboardingStep.AddToFavorite, viewModel.uiState.value.step)
        assertEquals(ImageAccessLevel.Full, viewModel.uiState.value.imageAccessLevel)
    }

    @Test
    fun loginWithEmail_movesToPermissionGuide() {
        val viewModel = createViewModel()

        viewModel.onAction(OnboardingAction.LoginWithEmail)

        assertEquals(OnboardingStep.PermissionGuide, viewModel.uiState.value.step)
    }

    @Test
    fun loginWithKakao_movesToPermissionGuideOnSuccess() = runTest(testDispatcher) {
        val context = mockk<Context>(relaxed = true)
        val authRepository = mockk<AuthRepository>()
        coEvery { authRepository.signInWithKakao(context) } returns Result.success(
            AuthSignInResult.Success(
                accessToken = "access",
                refreshToken = "refresh",
                accessTokenExpiresAt = "2026-07-10T13:00:00Z",
            ),
        )
        val viewModel = createViewModel(authRepository = authRepository)

        viewModel.loginWithKakao(context)
        advanceUntilIdle()

        assertEquals(OnboardingStep.PermissionGuide, viewModel.uiState.value.step)
        assertFalse(viewModel.uiState.value.isLoading)
        coVerify(exactly = 1) { authRepository.signInWithKakao(context) }
    }

    @Test
    fun loginWithKakao_staysOnLandingWhenCancelled() = runTest(testDispatcher) {
        val context = mockk<Context>(relaxed = true)
        val authRepository = mockk<AuthRepository>()
        coEvery { authRepository.signInWithKakao(context) } returns Result.failure(
            AuthException(AuthError.Cancelled),
        )
        val viewModel = createViewModel(authRepository = authRepository)
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
        val authRepository = mockk<AuthRepository>()
        coEvery { authRepository.signInWithKakao(context) } returns Result.failure(
            AuthException(AuthError.Server(code = "OAUTH_VERIFICATION_FAILED", message = "실패")),
        )
        val viewModel = createViewModel(authRepository = authRepository)
        val eventDeferred = async { viewModel.events.first() }

        viewModel.loginWithKakao(context)
        advanceUntilIdle()

        assertEquals(OnboardingStep.Landing, viewModel.uiState.value.step)
        val errorEvent = eventDeferred.await() as OnboardingEvent.ShowLoginError
        assertEquals(false, errorEvent.isCancelled)
    }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        imageAccessLevel: ImageAccessLevel = ImageAccessLevel.Denied,
        authRepository: AuthRepository = mockk(relaxed = true),
    ): OnboardingViewModel =
        OnboardingViewModel(
            savedStateHandle = savedStateHandle,
            imagePermissionRepository = FakeImagePermissionRepository(imageAccessLevel),
            authRepository = authRepository,
        )
}

private class FakeImagePermissionRepository(
    private val imageAccessLevel: ImageAccessLevel = ImageAccessLevel.Denied,
) : ImagePermissionRepository {
    override fun imagePermissionRequest(): Array<String> = emptyArray()

    override fun currentImageAccessLevel(): ImageAccessLevel = imageAccessLevel
}

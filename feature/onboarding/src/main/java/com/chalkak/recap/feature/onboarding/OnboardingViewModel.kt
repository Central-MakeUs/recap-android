package com.chalkak.recap.feature.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.auth.AuthException
import com.chalkak.recap.core.data.auth.AuthRepository
import com.chalkak.recap.core.data.network.NetworkConnectivityMonitor
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.network.TokenRefreshCoordinator
import com.chalkak.recap.core.data.screenshot.permission.ImagePermissionRepository
import com.chalkak.recap.core.model.ImageAccessLevel
import com.chalkak.recap.core.model.auth.AuthError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val imagePermissionRepository: ImagePermissionRepository,
    private val authRepository: AuthRepository,
    private val sessionTokenStore: SessionTokenStore,
    private val tokenRefreshCoordinator: TokenRefreshCoordinator,
    private val networkConnectivityMonitor: NetworkConnectivityMonitor,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()
    private val _illustrationSignals = MutableSharedFlow<OnboardingIllustrationSignal>(
        extraBufferCapacity = 1,
    )
    val illustrationSignals: SharedFlow<OnboardingIllustrationSignal> =
        _illustrationSignals.asSharedFlow()
    private val _events = MutableSharedFlow<OnboardingEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<OnboardingEvent> = _events.asSharedFlow()
    private var hasStepTransitionRequest = false

    init {
        refreshImagePermissionLevel()
        viewModelScope.launch {
            val initialStep = resolveInitialStep()
            if (!hasStepTransitionRequest) {
                applyStep(initialStep)
            }
        }
    }

    fun imagePermissionRequest(): Array<String> = imagePermissionRepository.imagePermissionRequest()

    fun broadcastIllustrationSignal(signal: OnboardingIllustrationSignal) {
        _illustrationSignals.tryEmit(signal)
    }

    fun refreshImagePermission(): ImageAccessLevel = refreshImagePermissionLevel()

    fun refreshImagePermissionAndMove(): ImageAccessLevel {
        val accessLevel = refreshImagePermissionLevel()
        moveTo(
            if (accessLevel == ImageAccessLevel.Full) {
                OnboardingStep.StartFirstAnalyze
            } else {
                OnboardingStep.AddToFavorite
            }
        )
        return accessLevel
    }

    fun refreshImagePermissionAndMoveToFirstOrganize(): ImageAccessLevel {
        val accessLevel = refreshImagePermissionLevel()
        markPermissionStepResolved()
        moveTo(OnboardingStep.UploadMethodGuide)
        return accessLevel
    }

    fun loginWithKakao(context: Context) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            if (!networkConnectivityMonitor.isInternetValidated()) {
                _events.emit(OnboardingEvent.ShowNoInternet)
                return@launch
            }

            _uiState.update { current ->
                current.copy(isLoading = true, errorMessage = null)
            }

            authRepository.signInWithKakao(context).fold(
                onSuccess = {
                    _uiState.update { current -> current.copy(isLoading = false) }
                    proceedAfterLogin()
                },
                onFailure = { error ->
                    val authError = (error as? AuthException)?.authError ?: AuthError.Unknown
                    _uiState.update { current -> current.copy(isLoading = false) }
                    _events.emit(loginFailureEvent(authError))
                },
            )
        }
    }

    fun onAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.Back -> moveBack()
            OnboardingAction.LoginWithKakao -> Unit

            OnboardingAction.SelectFirstScreenshots -> {
                val accessLevel = refreshImagePermissionLevel()
                moveTo(
                    if (accessLevel == ImageAccessLevel.Full) {
                        OnboardingStep.StartFirstAnalyze
                    } else {
                        OnboardingStep.AddToFavorite
                    }
                )
            }
            OnboardingAction.OpenAddToFavoriteGuide -> Unit
            OnboardingAction.CompleteAddToFavorite -> moveTo(OnboardingStep.StartFirstAnalyze)
            OnboardingAction.SkipFirstOrganize -> moveTo(OnboardingStep.StartFirstAnalyze)

            OnboardingAction.GrantPermission -> Unit
            OnboardingAction.OpenPhotoPermissionSettings -> Unit
            OnboardingAction.RefreshImagePermission -> refreshImagePermissionAndMove()

            OnboardingAction.SkipPermission -> refreshImagePermissionAndMoveToFirstOrganize()

            OnboardingAction.OpenScreenshotPicker,
            OnboardingAction.SkipStartFirstAnalyze -> Unit

            OnboardingAction.ConfirmUploadMethodGuide -> moveTo(OnboardingStep.AddToFavorite)
            is OnboardingAction.SelectStep -> moveTo(action.step)
        }
    }

    private fun proceedAfterLogin() {
        moveTo(stepAfterPermissionResolved(refreshImagePermissionLevel()))
    }

    private fun refreshImagePermissionLevel(): ImageAccessLevel {
        val accessLevel = imagePermissionRepository.currentImageAccessLevel()
        _uiState.update { current ->
            current.copy(
                imageAccessLevel = accessLevel,
            )
        }

        return accessLevel
    }

    private fun moveTo(step: OnboardingStep) {
        hasStepTransitionRequest = true
        applyStep(step)
    }

    private fun applyStep(step: OnboardingStep) {
        if (_uiState.value.step == step) return
        _uiState.update { current ->
            current.copy(step = step, errorMessage = null)
        }
    }

    private fun moveBack() {
        moveTo(_uiState.value.step.previousStep())
    }

    private suspend fun resolveInitialStep(): OnboardingStep {
        val refreshToken = sessionTokenStore.getRefreshToken()
        if (refreshToken == null) {
            return OnboardingStep.Landing
        }

        val refreshed = tokenRefreshCoordinator.refreshIfNeeded(force = true)
        if (!refreshed && sessionTokenStore.getRefreshToken() == null) {
            return OnboardingStep.Landing
        }

        return stepAfterPermissionResolved(refreshImagePermissionLevel())
    }

    private fun stepAfterPermissionResolved(accessLevel: ImageAccessLevel): OnboardingStep {
        return if (accessLevel == ImageAccessLevel.Denied) {
            OnboardingStep.PermissionGuide
        } else {
            markPermissionStepResolved()
            OnboardingStep.UploadMethodGuide
        }
    }

    private fun markPermissionStepResolved() {
        _uiState.update { current ->
            current.copy(hasResolvedPermissionStep = true)
        }
    }

    private fun loginFailureEvent(authError: AuthError): OnboardingEvent =
        when (authError) {
            AuthError.Network -> OnboardingEvent.ShowNoInternet
            AuthError.Cancelled -> OnboardingEvent.ShowLoginError(isCancelled = true)
            else -> OnboardingEvent.ShowLoginError(isCancelled = false)
        }
}

private fun OnboardingStep.previousStep(): OnboardingStep =
    when (this) {
        OnboardingStep.Landing -> OnboardingStep.Landing
        OnboardingStep.PermissionGuide -> OnboardingStep.PermissionGuide
        OnboardingStep.UploadMethodGuide -> OnboardingStep.PermissionGuide
        OnboardingStep.AddToFavorite -> OnboardingStep.UploadMethodGuide
        OnboardingStep.StartFirstAnalyze -> OnboardingStep.AddToFavorite
    }

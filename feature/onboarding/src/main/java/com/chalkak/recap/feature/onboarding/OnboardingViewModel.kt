package com.chalkak.recap.feature.onboarding

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.auth.AuthException
import com.chalkak.recap.core.data.auth.AuthRepository
import com.chalkak.recap.core.data.screenshot.ImagePermissionRepository
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
    private val savedStateHandle: SavedStateHandle,
    private val imagePermissionRepository: ImagePermissionRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        OnboardingUiState(step = savedStateHandle.restoreOnboardingStep()),
    )
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()
    private val _illustrationSignals = MutableSharedFlow<OnboardingIllustrationSignal>(
        extraBufferCapacity = 1,
    )
    val illustrationSignals: SharedFlow<OnboardingIllustrationSignal> =
        _illustrationSignals.asSharedFlow()
    private val _events = MutableSharedFlow<OnboardingEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<OnboardingEvent> = _events.asSharedFlow()

    init {
        refreshImagePermissionLevel()
    }

    fun imagePermissionRequest(): Array<String> = imagePermissionRepository.imagePermissionRequest()

    fun broadcastIllustrationSignal(signal: OnboardingIllustrationSignal) {
        _illustrationSignals.tryEmit(signal)
    }

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
        moveTo(OnboardingStep.UploadMethodGuide)
        return accessLevel
    }

    fun loginWithKakao(context: Context) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
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
                    _events.emit(
                        OnboardingEvent.ShowLoginError(
                            isCancelled = authError == AuthError.Cancelled,
                        ),
                    )
                },
            )
        }
    }

    fun onAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.Back -> moveBack()
            OnboardingAction.LoginWithKakao -> Unit
            OnboardingAction.LoginWithEmail -> proceedAfterLogin()

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
            OnboardingAction.SkipFirstOrganize -> moveTo(OnboardingStep.StartFirstAnalyze)

            OnboardingAction.GrantPermission -> Unit
            OnboardingAction.OpenPhotoPermissionSettings -> Unit
            OnboardingAction.RefreshImagePermission -> refreshImagePermissionAndMove()

            OnboardingAction.SkipPermission -> refreshImagePermissionAndMoveToFirstOrganize()

            OnboardingAction.OpenScreenshotPicker,
            OnboardingAction.SkipStartFirstAnalyze -> Unit

            OnboardingAction.ConfirmUploadMethodGuide -> moveTo(OnboardingStep.AddToFavorite)
        }
    }

    private fun proceedAfterLogin() {
        refreshImagePermissionLevel()
        moveTo(OnboardingStep.PermissionGuide)
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
        savedStateHandle[ONBOARDING_STEP_SAVED_STATE_KEY] = step.name
        _uiState.update { current ->
            current.copy(step = step, errorMessage = null)
        }
    }

    private fun moveBack() {
        moveTo(_uiState.value.step.previousStep())
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

internal const val ONBOARDING_STEP_SAVED_STATE_KEY = "onboarding_step"

private fun SavedStateHandle.restoreOnboardingStep(): OnboardingStep {
    val savedStepName = get<String>(ONBOARDING_STEP_SAVED_STATE_KEY) ?: return OnboardingStep.Landing
    return runCatching { OnboardingStep.valueOf(savedStepName) }
        .getOrDefault(OnboardingStep.Landing)
}

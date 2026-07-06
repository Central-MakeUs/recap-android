package com.chalkak.recap.feature.onboarding

import androidx.lifecycle.ViewModel
import com.chalkak.recap.core.data.ocr.OcrRepository
import com.chalkak.recap.core.model.ImageAccessLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val ocrRepository: OcrRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()
    private val _illustrationSignals = MutableSharedFlow<OnboardingIllustrationSignal>(
        extraBufferCapacity = 1,
    )
    val illustrationSignals: SharedFlow<OnboardingIllustrationSignal> =
        _illustrationSignals.asSharedFlow()

    init {
        refreshImagePermissionLevel()
    }

    fun imagePermissionRequest(): Array<String> = ocrRepository.imagePermissionRequest()

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

    fun refreshImagePermissionAndMoveToFirstCleanup(): ImageAccessLevel {
        val accessLevel = refreshImagePermissionLevel()
        moveTo(OnboardingStep.AddToFavorite)
        return accessLevel
    }

    fun onAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.Back -> moveBack()
            OnboardingAction.LoginWithKakao,
            OnboardingAction.LoginWithApple,
            OnboardingAction.LoginWithEmail -> {
                refreshImagePermissionLevel()
                moveTo(OnboardingStep.PermissionGuide)
            }

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
            OnboardingAction.SkipFirstCleanup -> moveTo(OnboardingStep.StartFirstAnalyze)

            OnboardingAction.GrantPermission -> Unit
            OnboardingAction.OpenPhotoPermissionSettings -> Unit
            OnboardingAction.RefreshImagePermission -> refreshImagePermissionAndMove()

            OnboardingAction.SkipPermission -> refreshImagePermissionAndMoveToFirstCleanup()

            OnboardingAction.OpenScreenshotPicker,
            OnboardingAction.SkipStartFirstAnalyze -> Unit
        }
    }

    private fun refreshImagePermissionLevel(): ImageAccessLevel {
        val accessLevel = ocrRepository.currentImageAccessLevel()
        _uiState.update { current ->
            current.copy(
                imageAccessLevel = accessLevel,
            )
        }

        return accessLevel
    }

    private fun moveTo(step: OnboardingStep) {
        _uiState.update { current ->
            current.copy(step = step, errorMessage = null)
        }
    }

    private fun moveBack() {
        _uiState.update { current ->
            current.copy(step = current.step.previousStep(), errorMessage = null)
        }
    }
}

private fun OnboardingStep.previousStep(): OnboardingStep =
    when (this) {
        OnboardingStep.Landing -> OnboardingStep.Landing
        OnboardingStep.PermissionGuide -> OnboardingStep.PermissionGuide
        OnboardingStep.AddToFavorite -> OnboardingStep.PermissionGuide
        OnboardingStep.StartFirstAnalyze -> OnboardingStep.AddToFavorite
    }

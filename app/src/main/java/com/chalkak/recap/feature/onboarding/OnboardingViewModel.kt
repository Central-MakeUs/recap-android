package com.chalkak.recap.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.ocr.OcrRepository
import com.chalkak.recap.core.model.ImageAccessLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val ocrRepository: OcrRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        refreshImagePermissionLevel()
        observeOcrJob()
    }

    fun imagePermissionRequest(): Array<String> = ocrRepository.imagePermissionRequest()

    fun refreshImagePermissionAndMove(): ImageAccessLevel {
        val accessLevel = refreshImagePermissionLevel()
        moveTo(
            if (accessLevel == ImageAccessLevel.Full) {
                OnboardingStep.CleanupRange
            } else {
                OnboardingStep.FirstCleanup
            }
        )
        return accessLevel
    }

    fun onAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.Back -> moveBack()
            OnboardingAction.StartOnboarding -> moveTo(OnboardingStep.ImagePolicy)
            OnboardingAction.ContinuePolicy -> moveTo(OnboardingStep.Login)
            OnboardingAction.OpenLogin -> moveTo(OnboardingStep.Login)
            OnboardingAction.LoginWithKakao,
            OnboardingAction.LoginWithApple,
            OnboardingAction.LoginWithEmail -> {
                refreshImagePermissionLevel()
                moveTo(OnboardingStep.FirstCleanup)
            }

            OnboardingAction.SelectFirstScreenshots -> {
                val accessLevel = refreshImagePermissionLevel()
                moveTo(
                    if (accessLevel == ImageAccessLevel.Full) {
                        OnboardingStep.CleanupRange
                    } else {
                        OnboardingStep.FirstCleanup
                    }
                )
            }
            OnboardingAction.SkipFirstCleanup -> Unit

            OnboardingAction.GrantPermission -> Unit
            OnboardingAction.OpenPhotoPermissionSettings -> Unit
            OnboardingAction.RefreshImagePermission -> refreshImagePermissionAndMove()

            OnboardingAction.SkipPermission -> refreshImagePermissionAndMove()

            is OnboardingAction.SelectRange -> {
                _uiState.update { current ->
                    current.copy(selectedRange = action.range)
                }
            }

            OnboardingAction.ConfirmRange -> startOcrAndMoveToCleanup()
            OnboardingAction.StartCleanup -> Unit
        }
    }

    private fun observeOcrJob() {
        viewModelScope.launch {
            ocrRepository.observeLatestJob().collect { job ->
                _uiState.update { current ->
                    current.copy(activeOcrJob = job)
                }
            }
        }
    }

    private fun refreshImagePermissionLevel(): ImageAccessLevel {
        val accessLevel = ocrRepository.currentImageAccessLevel()
        _uiState.update { current ->
            current.copy(
                imageAccessLevel = accessLevel,
                isRangeCountLoading = accessLevel != ImageAccessLevel.Denied,
                rangeCounts = if (accessLevel == ImageAccessLevel.Denied) {
                    CleanupRange.entries.associateWith { 0 }
                } else {
                    current.rangeCounts
                },
            )
        }

        if (accessLevel == ImageAccessLevel.Denied) {
            return accessLevel
        }

        viewModelScope.launch {
            val counts = CleanupRange.entries.associateWith { range ->
                ocrRepository.countScreenshots(range.ocrRange)
            }
            _uiState.update { current ->
                current.copy(
                    rangeCounts = counts,
                    isRangeCountLoading = false,
                    errorMessage = null,
                )
            }
        }

        return accessLevel
    }

    private fun startOcrAndMoveToCleanup() {
        val selectedRange = _uiState.value.selectedRange
        if (!_uiState.value.canConfirmRange) {
            _uiState.update { current ->
                current.copy(errorMessage = "cleanup_range_unavailable")
            }
            return
        }

        _uiState.update { current -> current.copy(activeOcrJob = null) }
        moveTo(OnboardingStep.CleanupStart)
        viewModelScope.launch {
            _uiState.update { current -> current.copy(isLoading = true, errorMessage = null) }
            runCatching {
                ocrRepository.startOcr(selectedRange.ocrRange)
            }.onFailure {
                _uiState.update { current ->
                    current.copy(errorMessage = "ocr_start_failed")
                }
            }
            _uiState.update { current -> current.copy(isLoading = false) }
        }
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
        OnboardingStep.ImagePolicy -> OnboardingStep.Landing
        OnboardingStep.Login -> OnboardingStep.ImagePolicy
        OnboardingStep.FirstCleanup -> OnboardingStep.Login
        OnboardingStep.CleanupRange -> OnboardingStep.FirstCleanup
        OnboardingStep.CleanupStart -> OnboardingStep.CleanupRange
    }

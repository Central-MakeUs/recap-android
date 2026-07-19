package com.chalkak.recap.feature.developer

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.UserPreferencesRepository
import com.chalkak.recap.core.data.screenshot.AnalysisDataSourceMode
import com.chalkak.recap.core.data.screenshot.ScreenshotAnalysisRunState
import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeveloperOptionsUiState(
    val analysisDataSourceMode: AnalysisDataSourceMode = AnalysisDataSourceMode.MOCK,
    val isAnalysisRunning: Boolean = false,
    val isSwitching: Boolean = false,
    val pendingSwitchTargetMode: AnalysisDataSourceMode? = null,
    @get:StringRes val feedbackMessageResId: Int? = null,
) {
    val canSwitchAnalysisDataSource: Boolean
        get() = !isAnalysisRunning && !isSwitching
}

@HiltViewModel
class DeveloperViewModel @Inject constructor(
    private val screenshotCardRepository: ScreenshotCardRepository,
    private val screenshotImageStorage: ScreenshotImageStorage,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val screenshotAnalysisRunState: ScreenshotAnalysisRunState,
) : ViewModel() {
    private val isSwitching = MutableStateFlow(false)
    private val pendingSwitchTargetMode = MutableStateFlow<AnalysisDataSourceMode?>(null)
    private val feedbackMessageResId = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<DeveloperOptionsUiState> = combine(
        userPreferencesRepository.analysisDataSourceMode,
        screenshotAnalysisRunState.isRunning,
        isSwitching,
        pendingSwitchTargetMode,
        feedbackMessageResId,
    ) { mode, running, switching, pendingTarget, feedback ->
        DeveloperOptionsUiState(
            analysisDataSourceMode = mode,
            isAnalysisRunning = running,
            isSwitching = switching,
            pendingSwitchTargetMode = pendingTarget,
            feedbackMessageResId = feedback,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = DeveloperOptionsUiState(),
    )

    internal fun onAction(action: DeveloperOptionAction) {
        when (action) {
            DeveloperOptionAction.OpenComponentGarden,
            DeveloperOptionAction.ResetOnboarding,
                -> Unit

            DeveloperOptionAction.ResetScreenshotData -> resetScreenshotData()
            is DeveloperOptionAction.RequestAnalysisDataSourceSwitch -> {
                requestAnalysisDataSourceSwitch(action.targetMode)
            }

            DeveloperOptionAction.ConfirmAnalysisDataSourceSwitch -> {
                confirmAnalysisDataSourceSwitch()
            }

            DeveloperOptionAction.DismissAnalysisDataSourceSwitchDialog -> {
                pendingSwitchTargetMode.value = null
            }
        }
    }

    fun resetScreenshotData() {
        viewModelScope.launch {
            val result = runCatching {
                screenshotCardRepository.deleteAllCards()
                screenshotImageStorage.clearStoredImages()
            }
            feedbackMessageResId.value = if (result.isSuccess) {
                com.chalkak.recap.core.design.R.string.developer_options_reset_screenshot_data_success
            } else {
                com.chalkak.recap.core.design.R.string.developer_options_reset_screenshot_data_failure
            }
        }
    }

    private fun requestAnalysisDataSourceSwitch(targetMode: AnalysisDataSourceMode) {
        val current = uiState.value
        if (targetMode == current.analysisDataSourceMode) {
            return
        }
        if (!current.canSwitchAnalysisDataSource) {
            feedbackMessageResId.value =
                com.chalkak.recap.core.design.R.string.developer_options_switch_analysis_data_source_rejected_busy
            return
        }
        pendingSwitchTargetMode.value = targetMode
    }

    private fun confirmAnalysisDataSourceSwitch() {
        val targetMode = pendingSwitchTargetMode.value ?: return
        if (screenshotAnalysisRunState.isRunning.value || isSwitching.value) {
            pendingSwitchTargetMode.value = null
            feedbackMessageResId.value =
                com.chalkak.recap.core.design.R.string.developer_options_switch_analysis_data_source_rejected_busy
            return
        }

        viewModelScope.launch {
            isSwitching.value = true
            pendingSwitchTargetMode.value = null
            val result = runCatching {
                screenshotCardRepository.deleteAllCards()
                screenshotImageStorage.clearStoredImages()
                userPreferencesRepository.setAnalysisDataSourceMode(targetMode)
            }
            feedbackMessageResId.value = if (result.isSuccess) {
                com.chalkak.recap.core.design.R.string.developer_options_switch_analysis_data_source_success
            } else {
                com.chalkak.recap.core.design.R.string.developer_options_switch_analysis_data_source_failure
            }
            isSwitching.value = false
        }
    }
}

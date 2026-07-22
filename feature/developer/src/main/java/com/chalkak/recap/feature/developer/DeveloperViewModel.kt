package com.chalkak.recap.feature.developer

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.screenshot.backend.MockScreenshotDataResetter
import com.chalkak.recap.core.data.screenshot.analysis.ScreenshotAnalysisRunState
import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendModeStore
import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendSwitchResult
import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendSwitcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeveloperOptionsUiState(
    val screenshotBackendMode: ScreenshotBackendMode = ScreenshotBackendMode.MOCK,
    val isAnalysisRunning: Boolean = false,
    val isSwitching: Boolean = false,
    val pendingSwitchTargetMode: ScreenshotBackendMode? = null,
    @get:StringRes val feedbackMessageResId: Int? = null,
) {
    val canSwitchScreenshotBackend: Boolean
        get() = !isAnalysisRunning && !isSwitching
}

@HiltViewModel
class DeveloperViewModel @Inject constructor(
    private val screenshotBackendModeStore: ScreenshotBackendModeStore,
    private val screenshotBackendSwitcher: ScreenshotBackendSwitcher,
    private val mockScreenshotDataResetter: MockScreenshotDataResetter,
    private val screenshotAnalysisRunState: ScreenshotAnalysisRunState,
) : ViewModel() {
    private val pendingSwitchTargetMode = MutableStateFlow<ScreenshotBackendMode?>(null)
    private val feedbackMessageResId = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<DeveloperOptionsUiState> = combine(
        screenshotBackendModeStore.mode,
        screenshotAnalysisRunState.isRunning,
        screenshotBackendSwitcher.isSwitching,
        pendingSwitchTargetMode,
        feedbackMessageResId,
    ) { mode, running, switching, pendingTarget, feedback ->
        DeveloperOptionsUiState(
            screenshotBackendMode = mode,
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
            is DeveloperOptionAction.RequestScreenshotBackendSwitch -> {
                requestScreenshotBackendSwitch(action.targetMode)
            }

            DeveloperOptionAction.ConfirmScreenshotBackendSwitch -> {
                confirmScreenshotBackendSwitch()
            }

            DeveloperOptionAction.DismissScreenshotBackendSwitchDialog -> {
                pendingSwitchTargetMode.value = null
            }
        }
    }

    fun resetScreenshotData() {
        viewModelScope.launch {
            val result = runCatching {
                mockScreenshotDataResetter.reset()
            }
            feedbackMessageResId.value = if (result.isSuccess) {
                com.chalkak.recap.core.design.R.string.developer_options_reset_screenshot_data_success
            } else {
                com.chalkak.recap.core.design.R.string.developer_options_reset_screenshot_data_failure
            }
        }
    }

    private fun requestScreenshotBackendSwitch(targetMode: ScreenshotBackendMode) {
        val current = uiState.value
        if (targetMode == current.screenshotBackendMode) {
            return
        }
        if (!current.canSwitchScreenshotBackend) {
            feedbackMessageResId.value =
                com.chalkak.recap.core.design.R.string.developer_options_switch_screenshot_backend_rejected_busy
            return
        }
        pendingSwitchTargetMode.value = targetMode
    }

    private fun confirmScreenshotBackendSwitch() {
        val targetMode = pendingSwitchTargetMode.value ?: return
        if (screenshotAnalysisRunState.isRunning.value || screenshotBackendSwitcher.isSwitching.value) {
            pendingSwitchTargetMode.value = null
            feedbackMessageResId.value =
                com.chalkak.recap.core.design.R.string.developer_options_switch_screenshot_backend_rejected_busy
            return
        }

        viewModelScope.launch {
            pendingSwitchTargetMode.value = null
            feedbackMessageResId.value = when (screenshotBackendSwitcher.switchTo(targetMode)) {
                ScreenshotBackendSwitchResult.Success ->
                    com.chalkak.recap.core.design.R.string.developer_options_switch_screenshot_backend_success

                ScreenshotBackendSwitchResult.RejectedBusy ->
                    com.chalkak.recap.core.design.R.string.developer_options_switch_screenshot_backend_rejected_busy

                ScreenshotBackendSwitchResult.Failure ->
                    com.chalkak.recap.core.design.R.string.developer_options_switch_screenshot_backend_failure
            }
        }
    }
}

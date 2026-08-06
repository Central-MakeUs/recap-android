package com.chalkak.recap.feature.developer

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.screenshot.backend.MockScreenshotDataResetter
import com.chalkak.recap.core.design.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeveloperOptionsUiState(
    @get:StringRes val feedbackMessageResId: Int? = null,
)

@HiltViewModel
class DeveloperViewModel @Inject constructor(
    private val mockScreenshotDataResetter: MockScreenshotDataResetter,
    private val sessionTokenStore: SessionTokenStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DeveloperOptionsUiState())
    val uiState: StateFlow<DeveloperOptionsUiState> = _uiState.asStateFlow()

    internal fun onAction(action: DeveloperOptionAction) {
        when (action) {
            DeveloperOptionAction.OpenComponentGarden,
            DeveloperOptionAction.ResetOnboarding,
                -> Unit

            DeveloperOptionAction.InvalidateSession -> invalidateSession()
            DeveloperOptionAction.ResetScreenshotData -> resetScreenshotData()
            DeveloperOptionAction.ForceTestCrash -> {
                throw RuntimeException("Test Crash")
            }
        }
    }

    fun invalidateSession() {
        viewModelScope.launch {
            val result = runCatching {
                sessionTokenStore.clear()
            }
            _uiState.update {
                it.copy(
                    feedbackMessageResId = if (result.isSuccess) {
                        R.string.developer_options_invalidate_session_success
                    } else {
                        R.string.developer_options_invalidate_session_failure
                    },
                )
            }
        }
    }

    fun resetScreenshotData() {
        viewModelScope.launch {
            val result = runCatching {
                mockScreenshotDataResetter.reset()
            }
            _uiState.update {
                it.copy(
                    feedbackMessageResId = if (result.isSuccess) {
                        R.string.developer_options_reset_screenshot_data_success
                    } else {
                        R.string.developer_options_reset_screenshot_data_failure
                    },
                )
            }
        }
    }
}

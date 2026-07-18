package com.chalkak.recap.feature.developer

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
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
    private val screenshotCardRepository: ScreenshotCardRepository,
    private val screenshotImageStorage: ScreenshotImageStorage,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DeveloperOptionsUiState())
    val uiState: StateFlow<DeveloperOptionsUiState> = _uiState.asStateFlow()

    fun resetScreenshotData() {
        viewModelScope.launch {
            val result = runCatching {
                screenshotCardRepository.deleteAllCards()
                screenshotImageStorage.clearStoredImages()
            }
            _uiState.update {
                it.copy(
                    feedbackMessageResId = if (result.isSuccess) {
                        com.chalkak.recap.core.design.R.string.developer_options_reset_screenshot_data_success
                    } else {
                        com.chalkak.recap.core.design.R.string.developer_options_reset_screenshot_data_failure
                    },
                )
            }
        }
    }
}

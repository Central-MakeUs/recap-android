package com.chalkak.recap.feature.developer

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.ocr.OcrRepository
import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.model.OcrImageResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DeveloperOptionsUiState(
    val ocrRawResults: List<OcrImageResult> = emptyList(),
    @get:StringRes val feedbackMessageResId: Int? = null,
)

@HiltViewModel
class DeveloperViewModel @Inject constructor(
    ocrRepository: OcrRepository,
    private val screenshotCardRepository: ScreenshotCardRepository,
    private val screenshotImageStorage: ScreenshotImageStorage,
) : ViewModel() {
    private val feedbackMessageResId = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<DeveloperOptionsUiState> =
        combine(
            ocrRepository.observeLatestJob()
                .map { job -> job?.results.orEmpty() },
            feedbackMessageResId,
        ) { ocrRawResults, feedback ->
            DeveloperOptionsUiState(
                ocrRawResults = ocrRawResults,
                feedbackMessageResId = feedback,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DeveloperOptionsUiState(),
        )

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
}

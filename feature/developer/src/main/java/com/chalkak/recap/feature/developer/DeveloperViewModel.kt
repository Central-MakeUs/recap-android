package com.chalkak.recap.feature.developer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.ocr.OcrRepository
import com.chalkak.recap.core.model.OcrImageResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DeveloperOptionsUiState(
    val ocrRawResults: List<OcrImageResult> = emptyList(),
)

@HiltViewModel
class DeveloperViewModel @Inject constructor(
    ocrRepository: OcrRepository,
) : ViewModel() {
    val uiState: StateFlow<DeveloperOptionsUiState> =
        ocrRepository.observeLatestJob()
            .map { job -> DeveloperOptionsUiState(ocrRawResults = job?.results.orEmpty()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DeveloperOptionsUiState(),
            )
}

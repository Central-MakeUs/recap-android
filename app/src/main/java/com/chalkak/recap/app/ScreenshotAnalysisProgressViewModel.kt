package com.chalkak.recap.app

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.screenshot.ScreenshotAnalysisInput
import com.chalkak.recap.core.data.screenshot.ScreenshotAnalysisRepository
import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardImageRefs
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScreenshotAnalysisProgressUiState(
    val isRunning: Boolean = false,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val progress: Float = 0f,
    val results: List<ScreenshotAnalysisResult> = emptyList(),
)

@HiltViewModel
class ScreenshotAnalysisProgressViewModel @Inject constructor(
    private val screenshotAnalysisRepository: ScreenshotAnalysisRepository,
    private val screenshotCardRepository: ScreenshotCardRepository,
    private val screenshotImageStorage: ScreenshotImageStorage,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScreenshotAnalysisProgressUiState())
    val uiState: StateFlow<ScreenshotAnalysisProgressUiState> = _uiState.asStateFlow()

    private var analysisJob: Job? = null

    fun startMockAnalysis(images: List<LocalImage>) {
        analysisJob?.cancel()
        val totalCount = images.size
        analysisJob = viewModelScope.launch {
            _uiState.value = ScreenshotAnalysisProgressUiState(
                isRunning = true,
                completedCount = 0,
                totalCount = totalCount,
                progress = if (totalCount == 0) 1f else 0f,
            )

            if (totalCount == 0) {
                _uiState.value = _uiState.value.copy(isRunning = false, progress = 1f)
                return@launch
            }

            val results = mutableListOf<ScreenshotAnalysisResult>()
            images.forEachIndexed { index, image ->
                delay(MOCK_ANALYSIS_DELAY_MILLIS)
                val result = screenshotAnalysisRepository.analyze(
                    ScreenshotAnalysisInput(fileName = image.displayName),
                )
                persistAnalysisResult(image = image, result = result)
                results.add(result)
                val completedCount = index + 1
                _uiState.value = _uiState.value.copy(
                    completedCount = completedCount,
                    progress = (completedCount.toFloat() / totalCount).coerceIn(0f, 1f),
                    results = results.toList(),
                )
            }

            _uiState.value = _uiState.value.copy(isRunning = false, progress = 1f)
        }
    }

    private suspend fun persistAnalysisResult(
        image: LocalImage,
        result: ScreenshotAnalysisResult,
    ) {
        val copiedPath = runCatching {
            screenshotImageStorage.copyImageFromUri(
                imageId = result.imageId,
                sourceUri = Uri.parse(image.uri),
            )
        }.getOrNull()
        val imageRefs = ScreenshotCardImageRefs(
            sourceImageUri = image.uri,
            storedImagePath = copiedPath,
            thumbnailPath = null,
        )
        runCatching {
            screenshotCardRepository.saveAnalysisResults(
                results = listOf(result),
                imageRefsByImageId = mapOf(result.imageId to imageRefs),
            )
        }
    }

    private companion object {
        const val MOCK_ANALYSIS_DELAY_MILLIS = 500L
    }
}

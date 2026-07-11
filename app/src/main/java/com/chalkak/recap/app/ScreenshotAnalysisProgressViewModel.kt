package com.chalkak.recap.app

import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

data class ScreenshotAnalysisProgressUiState(
    val isRunning: Boolean = false,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val progress: Float = 0f,
    val results: List<ScreenshotAnalysisResult> = emptyList(),
    val errorMessage: String? = null,
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

    // 추후 Dispatcher DI로 개선 가능
    @VisibleForTesting
    internal var ioDispatcher: CoroutineDispatcher = Dispatchers.IO

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
            images.forEachIndexed { _, image ->
                delay(MOCK_ANALYSIS_DELAY_MILLIS.milliseconds)
                ensureActive()

                val result = screenshotAnalysisRepository.analyze(
                    ScreenshotAnalysisInput(fileName = image.displayName),
                )
                val saved = persistAnalysisResult(image = image, result = result)
                if (!saved) {
                    if (!isActive) {
                        return@launch
                    }
                    _uiState.value = _uiState.value.copy(errorMessage = SAVE_ERROR_MESSAGE)
                    return@forEachIndexed
                }

                results.add(result)
                val completedCount = results.size
                if (!isActive) {
                    return@launch
                }
                _uiState.value = _uiState.value.copy(
                    completedCount = completedCount,
                    progress = (completedCount.toFloat() / totalCount).coerceIn(0f, 1f),
                    results = results.toList(),
                )
            }

            if (!isActive) {
                return@launch
            }
            val completedCount = results.size
            _uiState.value = _uiState.value.copy(
                isRunning = false,
                progress = (completedCount.toFloat() / totalCount).coerceIn(0f, 1f),
            )
        }
    }

    private suspend fun persistAnalysisResult(
        image: LocalImage,
        result: ScreenshotAnalysisResult,
    ): Boolean {
        return withContext(ioDispatcher) {
            val copiedPath = try {
                screenshotImageStorage.copyImageFromUri(
                    imageId = result.imageId,
                    sourceUri = image.uri.toUri(),
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                null
            }

            val imageRefs = ScreenshotCardImageRefs(
                sourceImageUri = image.uri,
                storedImagePath = copiedPath,
                thumbnailPath = null,
            )

            try {
                screenshotCardRepository.saveAnalysisResults(
                    results = listOf(result),
                    imageRefsByImageId = mapOf(result.imageId to imageRefs),
                )
                true
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                false
            }
        }
    }

    private companion object {
        const val MOCK_ANALYSIS_DELAY_MILLIS = 500L
        const val SAVE_ERROR_MESSAGE = "Failed to save screenshot analysis result"
    }
}

package com.chalkak.recap.app

import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.screenshot.analysis.ScreenshotAnalysisInput
import com.chalkak.recap.core.data.screenshot.analysis.ScreenshotAnalysisRepository
import com.chalkak.recap.core.data.screenshot.analysis.ScreenshotAnalysisRunState
import com.chalkak.recap.core.data.screenshot.analysis.ScreenshotOrganizeOutcome
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
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

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
    private val screenshotAnalysisRunState: ScreenshotAnalysisRunState,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScreenshotAnalysisProgressUiState())
    val uiState: StateFlow<ScreenshotAnalysisProgressUiState> = _uiState.asStateFlow()

    private var analysisJob: Job? = null

    // 추후 Dispatcher DI로 개선 가능
    @VisibleForTesting
    internal var ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    fun startAnalysis(images: List<LocalImage>) {
        analysisJob?.cancel()
        val totalCount = images.size
        analysisJob = viewModelScope.launch {
            screenshotAnalysisRunState.beginRun()
            try {
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

                val inputs = images.map { image ->
                    ScreenshotAnalysisInput(
                        fileName = image.displayName,
                        uri = image.uri,
                    )
                }

                val outcome = screenshotAnalysisRepository.organize(inputs) { completed, total ->
                    if (!isActive) return@organize
                    val safeTotal = total.coerceAtLeast(1)
                    _uiState.value = _uiState.value.copy(
                        completedCount = completed.coerceIn(0, safeTotal),
                        totalCount = safeTotal,
                        progress = (completed.toFloat() / safeTotal).coerceIn(0f, 1f),
                    )
                }

                ensureActive()
                when (outcome) {
                    is ScreenshotOrganizeOutcome.LocalResults -> {
                        val persisted = mutableListOf<ScreenshotAnalysisResult>()
                        outcome.results.forEachIndexed { index, result ->
                            ensureActive()
                            val image = images[index]
                            val saved = persistAnalysisResult(image = image, result = result)
                            if (!saved) {
                                if (!isActive) {
                                    return@launch
                                }
                                _uiState.value = _uiState.value.copy(
                                    isRunning = false,
                                    errorMessage = SAVE_ERROR_MESSAGE,
                                    results = persisted.toList(),
                                )
                                return@launch
                            }
                            persisted += result
                        }
                        if (!isActive) {
                            return@launch
                        }
                        _uiState.value = _uiState.value.copy(
                            isRunning = false,
                            completedCount = persisted.size,
                            progress = (persisted.size.toFloat() / totalCount).coerceIn(0f, 1f),
                            results = persisted.toList(),
                        )
                    }

                    is ScreenshotOrganizeOutcome.RemoteCompleted -> {
                        val completed = outcome.successCount + outcome.failCount
                        _uiState.value = _uiState.value.copy(
                            isRunning = false,
                            completedCount = completed.coerceIn(0, totalCount),
                            totalCount = totalCount,
                            progress = (completed.toFloat() / totalCount).coerceIn(0f, 1f),
                            results = emptyList(),
                        )
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Exception) {
                Timber.e(throwable, "Screenshot analysis failed")
                _uiState.value = _uiState.value.copy(
                    isRunning = false,
                    errorMessage = ANALYSIS_ERROR_MESSAGE,
                )
            } finally {
                screenshotAnalysisRunState.endRun()
            }
        }
    }

    private suspend fun persistAnalysisResult(
        image: LocalImage,
        result: ScreenshotAnalysisResult,
    ): Boolean {
        return withContext(ioDispatcher) {
            val sourceUri = try {
                image.uri.toUri()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                null
            }

            val copiedPath = if (sourceUri == null) {
                null
            } else {
                try {
                    screenshotImageStorage.copyImageFromUri(
                        captureId = result.captureId,
                        sourceUri = sourceUri,
                    )
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    null
                }
            }

            val thumbnailPath = if (sourceUri == null) {
                null
            } else {
                try {
                    if (copiedPath != null) {
                        screenshotImageStorage.createThumbnailFromStoredImage(result.captureId)
                    } else {
                        screenshotImageStorage.createThumbnailFromUri(
                            captureId = result.captureId,
                            sourceUri = sourceUri,
                        )
                    }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (throwable: Exception) {
                    Timber.w(throwable, "Failed to create screenshot thumbnail")
                    null
                }
            }

            val imageRefs = ScreenshotCardImageRefs(
                sourceImageUri = image.uri,
                storedImagePath = copiedPath,
                thumbnailPath = thumbnailPath,
            )

            if (thumbnailPath != null) {
                Timber.d(
                    "Persisting screenshot with thumbnail captureId=%s path=%s",
                    result.captureId,
                    thumbnailPath,
                )
            } else {
                Timber.d(
                    "Persisting screenshot without thumbnail captureId=%s storedImagePath=%s",
                    result.captureId,
                    copiedPath,
                )
            }

            try {
                screenshotCardRepository.saveAnalysisResults(
                    results = listOf(result),
                    imageRefsByCaptureId = mapOf(result.captureId to imageRefs),
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
        const val SAVE_ERROR_MESSAGE = "Failed to save screenshot analysis result"
        const val ANALYSIS_ERROR_MESSAGE = "Failed to analyze screenshot"
    }
}

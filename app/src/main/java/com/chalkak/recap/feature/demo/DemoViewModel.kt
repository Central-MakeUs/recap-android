package com.chalkak.recap.feature.demo

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.R
import com.chalkak.recap.core.data.LocalScreenshotDataSource
import com.chalkak.recap.core.data.ai.RecapAnalysisRepository
import com.chalkak.recap.core.data.ai.RecapAnalysisScreenshotInput
import com.chalkak.recap.core.model.ImageAccessLevel
import com.chalkak.recap.core.model.RecapAnalysisBatchResult
import com.chalkak.recap.core.model.RecapAnalysisInputMode
import com.chalkak.recap.core.model.RecapAnalysisRequestMode
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class DemoViewModel @Inject constructor(
    application: Application,
    private val screenshotDataSource: LocalScreenshotDataSource,
    private val recapAnalysisRepository: RecapAnalysisRepository,
) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private var ocrJob: Job? = null
    private var analysisJob: Job? = null

    private val _uiState = MutableStateFlow(
        DemoUiState(
            imagePermissionLevel = screenshotDataSource.currentImageAccessLevel().toDemoPermissionLevel(),
        ),
    )
    val uiState: StateFlow<DemoUiState> = _uiState.asStateFlow()

    init {
        loadRecentScreenshots(_uiState.value.imagePermissionLevel)
    }

    fun imagePermissionRequest(): Array<String> {
        return screenshotDataSource.imagePermissionRequest()
    }

    fun onAction(action: DemoAction) {
        when (action) {
            DemoAction.RequestImagePermission,
            DemoAction.RefreshImagePermission,
                -> refreshImagePermissionLevel()

            is DemoAction.RunOcr -> runOcr(action.engine)
            is DemoAction.SelectAnalysisInputMode -> {
                _uiState.update { current ->
                    current.copy(selectedAnalysisInputMode = action.inputMode)
                }
            }

            is DemoAction.SelectAnalysisRequestMode -> {
                _uiState.update { current ->
                    current.copy(selectedAnalysisRequestMode = action.requestMode)
                }
            }

            DemoAction.RunGeminiAnalysis -> runGeminiAnalysis()
        }
    }

    fun refreshImagePermissionLevel() {
        val permissionLevel = screenshotDataSource.currentImageAccessLevel().toDemoPermissionLevel()
        _uiState.update { current ->
            current.copy(
                imagePermissionLevel = permissionLevel,
            )
        }
        loadRecentScreenshots(permissionLevel)
    }

    private fun loadRecentScreenshots(permissionLevel: ImagePermissionLevel) {
        if (permissionLevel == ImagePermissionLevel.Denied) {
            _uiState.update { current ->
                current.copy(recentScreenshotUris = emptyList())
            }
            return
        }

        viewModelScope.launch {
            val screenshotUris = screenshotDataSource
                .queryRecentScreenshots(RecentScreenshotLimit)
                .map { it.uri.toUri() }
            _uiState.update { current ->
                current.copy(recentScreenshotUris = screenshotUris)
            }
        }
    }

    private fun runOcr(engine: OcrEngine) {
        val screenshotUris = _uiState.value.recentScreenshotUris.take(RecentScreenshotLimit)
        if (screenshotUris.isEmpty()) {
            _uiState.update { current ->
                current.copy(
                    ocrState = OcrUiState(
                        engine = engine,
                        errorMessage = appContext.getString(R.string.demo_ocr_error_no_screenshots),
                    ),
                )
            }
            return
        }

        ocrJob?.cancel()
        ocrJob = viewModelScope.launch {
            try {
                recognizeScreenshots(
                    engine = engine,
                    screenshotUris = screenshotUris,
                    publishProgress = true,
                )
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) {
                    throw throwable
                }
                _uiState.update { current ->
                    current.copy(
                        ocrState = current.ocrState.copy(
                            isRunning = false,
                            errorMessage = appContext.getString(R.string.demo_ocr_error_processing),
                        ),
                    )
                }
            }
        }
    }

    private fun runGeminiAnalysis() {
        val screenshotUris = _uiState.value.recentScreenshotUris.take(RecentScreenshotLimit)
        val requestedInputMode = _uiState.value.selectedAnalysisInputMode
        val requestedRequestMode = _uiState.value.selectedAnalysisRequestMode
        if (screenshotUris.isEmpty()) {
            _uiState.update { current ->
                current.copy(
                    analysisState = RecapAnalysisUiState(
                        errorMessage = appContext.getString(R.string.demo_gemini_error_no_screenshots),
                    ),
                )
            }
            return
        }

        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            val startedAtMillis = System.currentTimeMillis()
            _uiState.update { current ->
                current.copy(
                    analysisState = RecapAnalysisUiState(isRunning = true),
                )
            }

            try {
                val ocrResults = ensureOcrResults(screenshotUris)
                val inputs = screenshotUris.mapIndexed { index, uri ->
                    val imageIndex = index + 1
                    RecapAnalysisScreenshotInput(
                        imageId = "screenshot_$imageIndex",
                        uri = uri,
                        ocrText = ocrResults.firstOrNull { it.imageIndex == imageIndex }?.text.orEmpty(),
                    )
                }
                val result = recapAnalysisRepository.analyze(
                    screenshots = inputs,
                    inputMode = requestedInputMode,
                    requestMode = requestedRequestMode,
                )
                _uiState.update { current ->
                    current.copy(
                        analysisState = RecapAnalysisUiState(
                            result = result,
                        ),
                        analysisHistory = current.analysisHistory.prependRunSummary(
                            inputMode = requestedInputMode,
                            requestMode = requestedRequestMode,
                            screenshotCount = screenshotUris.size,
                            durationMillis = System.currentTimeMillis() - startedAtMillis,
                            result = result,
                            errorMessage = null,
                        ),
                    )
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) {
                    throw throwable
                }
                val errorMessage = appContext.getString(R.string.demo_gemini_error_processing)
                _uiState.update { current ->
                    current.copy(
                        ocrState = current.ocrState.copy(isRunning = false),
                    )
                }
                _uiState.update { current ->
                    current.copy(
                        analysisState = RecapAnalysisUiState(
                            errorMessage = errorMessage,
                        ),
                        analysisHistory = current.analysisHistory.prependRunSummary(
                            inputMode = requestedInputMode,
                            requestMode = requestedRequestMode,
                            screenshotCount = screenshotUris.size,
                            durationMillis = System.currentTimeMillis() - startedAtMillis,
                            result = null,
                            errorMessage = errorMessage,
                        ),
                    )
                }
            }
        }
    }

    private suspend fun ensureOcrResults(screenshotUris: List<Uri>): List<OcrImageResult> {
        val currentResults = _uiState.value.ocrState.results
        val hasAllResults = currentResults.size >= screenshotUris.size &&
                screenshotUris.withIndex().all { (index, uri) ->
                    currentResults.any { result ->
                        result.imageIndex == index + 1 && result.imageUri == uri.toString()
                    }
                }

        if (hasAllResults) {
            return currentResults
        }

        return recognizeScreenshots(
            engine = OcrEngine.Korean,
            screenshotUris = screenshotUris,
            publishProgress = true,
        )
    }

    private suspend fun recognizeScreenshots(
        engine: OcrEngine,
        screenshotUris: List<Uri>,
        publishProgress: Boolean,
    ): List<OcrImageResult> {
        if (publishProgress) {
            _uiState.update { current ->
                current.copy(
                    ocrState = OcrUiState(
                        engine = engine,
                        isRunning = true,
                        totalCount = screenshotUris.size,
                    ),
                )
            }
        }

        val recognizer = engine.createTextRecognizer()
        val results = mutableListOf<OcrImageResult>()
        try {
            screenshotUris.forEachIndexed { index, uri ->
                yield()

                val result = withContext(Dispatchers.IO) {
                    appContext.recognizeScreenshotText(
                        recognizer = recognizer,
                        uri = uri,
                        imageIndex = index + 1,
                    )
                }
                results += result
                if (publishProgress) {
                    _uiState.update { current ->
                        current.copy(
                            ocrState = current.ocrState.copy(
                                completedCount = results.size,
                                results = results.toList(),
                            ),
                        )
                    }
                }
            }
            if (publishProgress) {
                _uiState.update { current ->
                    current.copy(
                        ocrState = current.ocrState.copy(
                            isRunning = false,
                            completedCount = screenshotUris.size,
                        ),
                    )
                }
            }
            return results
        } finally {
            recognizer.close()
        }
    }
}

private fun ImageAccessLevel.toDemoPermissionLevel(): ImagePermissionLevel {
    return when {
        this == ImageAccessLevel.Full -> ImagePermissionLevel.Full
        this == ImageAccessLevel.Selected -> ImagePermissionLevel.Selected
        else -> ImagePermissionLevel.Denied
    }
}

private fun OcrEngine.createTextRecognizer(): TextRecognizer {
    return when (this) {
        OcrEngine.Latin -> TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        OcrEngine.Korean -> TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
    }
}

@WorkerThread
private suspend fun Context.recognizeScreenshotText(
    recognizer: TextRecognizer,
    uri: Uri,
    imageIndex: Int,
): OcrImageResult {
    val image = InputImage.fromFilePath(this, uri)
    val text = recognizer.process(image).await()
    return text.toOcrImageResult(
        imageIndex = imageIndex,
        imageUri = uri.toString(),
    )
}

private fun Text.toOcrImageResult(
    imageIndex: Int,
    imageUri: String,
): OcrImageResult {
    return OcrImageResult(
        imageIndex = imageIndex,
        imageUri = imageUri,
        text = text,
        blocks = textBlocks.map { block ->
            OcrTextBlock(
                text = block.text,
                lines = block.lines.map { line -> line.text },
            )
        },
    )
}

private suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }
        addOnFailureListener { throwable ->
            continuation.resumeWithException(throwable)
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }
}

private fun List<RecapAnalysisRunSummary>.prependRunSummary(
    inputMode: RecapAnalysisInputMode,
    requestMode: RecapAnalysisRequestMode,
    screenshotCount: Int,
    durationMillis: Long,
    result: RecapAnalysisBatchResult?,
    errorMessage: String?,
): List<RecapAnalysisRunSummary> {
    val runSummary = RecapAnalysisRunSummary(
        runIndex = firstOrNull()?.runIndex?.plus(1) ?: 1,
        inputMode = inputMode,
        requestMode = requestMode,
        screenshotCount = screenshotCount,
        durationMillis = durationMillis,
        resultCount = result?.results?.size ?: 0,
        reviewRequiredCount = result?.results?.count { it.needsReview } ?: 0,
        errorMessage = errorMessage,
    )
    return (listOf(runSummary) + this).take(AnalysisHistoryLimit)
}

private const val RecentScreenshotLimit = 10
private const val AnalysisHistoryLimit = 12

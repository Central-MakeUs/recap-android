package com.chalkak.recap.app

import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.app.notification.OrganizeProgressTracker
import com.chalkak.recap.app.notification.OrganizeTerminalResult
import com.chalkak.recap.app.notification.OrganizeTerminalResultMapper
import com.chalkak.recap.core.data.UserPreferencesRepository
import com.chalkak.recap.core.data.screenshot.analysis.RemoteOrganizeFailedException
import com.chalkak.recap.core.data.screenshot.analysis.ScreenshotAnalysisInput
import com.chalkak.recap.core.data.screenshot.analysis.ScreenshotAnalysisRepository
import com.chalkak.recap.core.data.screenshot.analysis.ScreenshotAnalysisRunState
import com.chalkak.recap.core.data.screenshot.analysis.ScreenshotOrganizeOutcome
import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardImageRefs
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.core.model.PreparedScreenshot
import com.chalkak.recap.core.model.ScreenshotUploadCandidate
import com.chalkak.recap.core.model.observability.CrashReporter
import com.chalkak.recap.core.model.observability.ObservabilityKeys
import com.chalkak.recap.core.model.observability.OrganizeTraceEntry
import com.chalkak.recap.core.model.observability.PerformanceTrace
import com.chalkak.recap.core.model.observability.PerformanceTraceNames
import com.chalkak.recap.core.model.observability.PerformanceTracer
import com.chalkak.recap.core.model.observability.imageCountBucket
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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
    val terminalResult: OrganizeTerminalResult? = null,
) {
    val isStatusVisible: Boolean
        get() = isRunning || terminalResult != null
}

@HiltViewModel
class ScreenshotAnalysisProgressViewModel @Inject constructor(
    private val screenshotAnalysisRepository: ScreenshotAnalysisRepository,
    private val screenshotCardRepository: ScreenshotCardRepository,
    private val screenshotImageStorage: ScreenshotImageStorage,
    private val screenshotAnalysisRunState: ScreenshotAnalysisRunState,
    private val organizeProgressTracker: OrganizeProgressTracker,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val crashReporter: CrashReporter,
    private val performanceTracer: PerformanceTracer,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScreenshotAnalysisProgressUiState())
    val uiState: StateFlow<ScreenshotAnalysisProgressUiState> = _uiState.asStateFlow()

    val organizeCompleteNotificationEnabled: StateFlow<Boolean?> =
        userPreferencesRepository.organizeCompleteNotificationEnabled
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null,
            )

    private var analysisJob: Job? = null
    private var activeOrganizeTrace: PerformanceTrace? = null
    private var activeOrganizeRunToken: Any? = null

    // 추후 Dispatcher DI로 개선 가능
    @VisibleForTesting
    internal var ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    fun setOrganizeCompleteNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setOrganizeCompleteNotificationEnabled(enabled)
        }
    }

    fun startAnalysis(
        candidates: List<ScreenshotUploadCandidate>,
        entry: String = OrganizeTraceEntry.HOME_ORGANIZE,
    ) {
        analysisJob?.cancel()
        stopActiveOrganizeTrace(outcome = "cancel")
        val runToken = Any()
        activeOrganizeRunToken = runToken
        val totalCount = candidates.size
        analysisJob = viewModelScope.launch {
            screenshotAnalysisRunState.beginRun()
            val runId = organizeProgressTracker.onStarted(totalCount)
            val bucket = imageCountBucket(totalCount)
            crashReporter.setCustomKey(ObservabilityKeys.ORGANIZE_ACTIVE, true)
            crashReporter.setCustomKey(ObservabilityKeys.IMAGE_COUNT_BUCKET, bucket)
            crashReporter.setCustomKey(ObservabilityKeys.ORGANIZE_PHASE, "running")
            val trace = performanceTracer.startTrace(
                PerformanceTraceNames.ORGANIZE_REMOTE_END_TO_END,
            )
            activeOrganizeTrace = trace
            trace.putAttribute(ObservabilityKeys.IMAGE_COUNT_BUCKET, bucket)
            trace.putAttribute(ObservabilityKeys.ENTRY, entry)
            try {
                _uiState.value = ScreenshotAnalysisProgressUiState(
                    isRunning = true,
                    completedCount = 0,
                    totalCount = totalCount,
                    progress = if (totalCount == 0) 1f else 0f,
                )

                if (totalCount == 0) {
                    val emptySuccess = OrganizeTerminalResult.AllSuccess(successCount = 0)
                    _uiState.value = _uiState.value.copy(
                        isRunning = false,
                        progress = 1f,
                        terminalResult = emptySuccess,
                    )
                    organizeProgressTracker.onTerminal(
                        runId = runId,
                        result = emptySuccess,
                    )
                    finishOrganizeSuccess(trace, emptySuccess)
                    return@launch
                }

                val inputs = candidates.map { candidate ->
                    val prepared = candidate.preparedScreenshot
                    ScreenshotAnalysisInput(
                        fileName = candidate.localImage.displayName,
                        uri = candidate.localImage.uri,
                        jpegBytes = prepared?.jpegBytes,
                        contentType = prepared?.mimeType
                            ?: PreparedScreenshot.MIME_TYPE_JPEG,
                        localImage = candidate.localImage,
                        completedPreparationAttempts =
                            candidate.completedPreparationAttempts,
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
                    organizeProgressTracker.onProgress(runId, completed, safeTotal)
                }

                ensureActive()
                when (outcome) {
                    is ScreenshotOrganizeOutcome.LocalResults -> {
                        val persisted = mutableListOf<ScreenshotAnalysisResult>()
                        outcome.results.forEachIndexed { index, result ->
                            ensureActive()
                            val image = outcome.sourceImages.getOrNull(index)
                                ?: candidates.getOrNull(index)?.localImage
                                ?: return@forEachIndexed
                            val saved = persistAnalysisResult(image = image, result = result)
                            if (!saved) {
                                if (!isActive) {
                                    organizeProgressTracker.onCancelled(runId)
                                    finishOrganizeCancelled(trace)
                                    return@launch
                                }
                                val terminal = OrganizeTerminalResultMapper.fromLocalPersisted(
                                    persistedCount = persisted.size,
                                    totalCount = totalCount,
                                    saveFailed = true,
                                )
                                crashReporter.recordException(
                                    IllegalStateException(SAVE_ERROR_MESSAGE),
                                )
                                _uiState.value = _uiState.value.copy(
                                    isRunning = false,
                                    errorMessage = SAVE_ERROR_MESSAGE,
                                    results = persisted.toList(),
                                    terminalResult = terminal,
                                )
                                organizeProgressTracker.onTerminal(
                                    runId = runId,
                                    result = terminal,
                                )
                                finishOrganizeSuccess(trace, terminal)
                                return@launch
                            }
                            persisted += result
                        }
                        if (!isActive) {
                            organizeProgressTracker.onCancelled(runId)
                            finishOrganizeCancelled(trace)
                            return@launch
                        }
                        val failCount = outcome.preparationFailCount + outcome.analysisFailCount
                        val terminal = when {
                            persisted.isEmpty() && failCount > 0 ->
                                OrganizeTerminalResult.AllFailed

                            failCount > 0 ->
                                OrganizeTerminalResult.PartialSuccess(
                                    successCount = persisted.size,
                                    failCount = failCount,
                                )
                            else -> OrganizeTerminalResultMapper.fromLocalPersisted(
                                persistedCount = persisted.size,
                                totalCount = totalCount,
                                saveFailed = false,
                            )
                        }
                        if (failCount > 0) {
                            crashReporter.recordException(
                                IllegalStateException(
                                    "Screenshot organize failed preparation=${outcome.preparationFailCount} analysis=${outcome.analysisFailCount}",
                                ),
                            )
                        }
                        _uiState.value = _uiState.value.copy(
                            isRunning = false,
                            completedCount = (persisted.size + failCount)
                                .coerceIn(0, totalCount),
                            progress = 1f,
                            results = persisted.toList(),
                            terminalResult = terminal,
                        )
                        organizeProgressTracker.onTerminal(
                            runId = runId,
                            result = terminal,
                        )
                        finishOrganizeSuccess(trace, terminal)
                    }

                    is ScreenshotOrganizeOutcome.RemoteCompleted -> {
                        val completed = outcome.successCount + outcome.failCount
                        val terminal = OrganizeTerminalResultMapper.fromRemote(outcome)
                        _uiState.value = _uiState.value.copy(
                            isRunning = false,
                            completedCount = completed.coerceIn(0, totalCount),
                            totalCount = totalCount,
                            progress = (completed.toFloat() / totalCount).coerceIn(0f, 1f),
                            results = emptyList(),
                            terminalResult = terminal,
                        )
                        organizeProgressTracker.onTerminal(
                            runId = runId,
                            result = terminal,
                        )
                        finishOrganizeSuccess(trace, terminal)
                    }
                }
            } catch (cancellation: CancellationException) {
                organizeProgressTracker.onCancelled(runId)
                finishOrganizeCancelled(trace)
                throw cancellation
            } catch (throwable: Exception) {
                Timber.e(throwable, "Screenshot analysis failed")
                if (throwable !is RemoteOrganizeFailedException) {
                    crashReporter.recordException(throwable)
                }
                _uiState.value = _uiState.value.copy(
                    isRunning = false,
                    errorMessage = ANALYSIS_ERROR_MESSAGE,
                    terminalResult = OrganizeTerminalResult.AllFailed,
                )
                organizeProgressTracker.onTerminal(
                    runId = runId,
                    result = OrganizeTerminalResult.AllFailed,
                )
                finishOrganizeSuccess(trace, OrganizeTerminalResult.AllFailed)
            } finally {
                screenshotAnalysisRunState.endRun()
                if (activeOrganizeRunToken === runToken) {
                    activeOrganizeRunToken = null
                    crashReporter.setCustomKey(ObservabilityKeys.ORGANIZE_ACTIVE, false)
                    crashReporter.setCustomKey(ObservabilityKeys.ORGANIZE_PHASE, "idle")
                }
            }
        }
    }

    fun cancelAnalysis() {
        analysisJob?.cancel()
        analysisJob = null
        activeOrganizeRunToken = null
        stopActiveOrganizeTrace(outcome = "cancel")
        crashReporter.setCustomKey(ObservabilityKeys.ORGANIZE_ACTIVE, false)
        crashReporter.setCustomKey(ObservabilityKeys.ORGANIZE_PHASE, "idle")
        _uiState.value = ScreenshotAnalysisProgressUiState()
    }

    fun dismissResult() {
        if (_uiState.value.isRunning) return
        _uiState.value = ScreenshotAnalysisProgressUiState()
    }

    private fun finishOrganizeSuccess(
        trace: PerformanceTrace,
        terminal: OrganizeTerminalResult,
    ) {
        stopOrganizeTrace(trace = trace, outcome = terminal.toOutcomeAttribute())
    }

    private fun finishOrganizeCancelled(trace: PerformanceTrace) {
        stopOrganizeTrace(trace = trace, outcome = "cancel")
    }

    private fun stopActiveOrganizeTrace(outcome: String) {
        val trace = activeOrganizeTrace ?: return
        stopOrganizeTrace(trace = trace, outcome = outcome)
    }

    private fun stopOrganizeTrace(
        trace: PerformanceTrace,
        outcome: String,
    ) {
        if (activeOrganizeTrace !== trace) return
        activeOrganizeTrace = null
        trace.putAttribute(ObservabilityKeys.OUTCOME, outcome)
        trace.stop()
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

        fun OrganizeTerminalResult.toOutcomeAttribute(): String =
            when (this) {
                is OrganizeTerminalResult.AllSuccess -> "success"
                is OrganizeTerminalResult.PartialSuccess -> "partial"
                OrganizeTerminalResult.AllFailed -> "fail"
            }
    }
}

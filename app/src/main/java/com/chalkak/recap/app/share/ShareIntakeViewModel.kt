package com.chalkak.recap.app.share

import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.UserPreferencesRepository
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.core.model.ScreenshotUploadCandidate
import com.chalkak.recap.core.model.observability.CrashReporter
import com.chalkak.recap.core.model.observability.ObservabilityKeys
import com.chalkak.recap.core.model.observability.PerformanceTrace
import com.chalkak.recap.core.model.observability.PerformanceTraceNames
import com.chalkak.recap.core.model.observability.PerformanceTracer
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ShareIntakeEvent {
    data object LoginRequired : ShareIntakeEvent

    data object OnboardingRequired : ShareIntakeEvent

    data object ReturnAfterOnboardingSampleShare : ShareIntakeEvent

    data class LaunchMainAnalysis(
        val requestId: String,
        val images: List<LocalImage>,
    ) : ShareIntakeEvent
}

@HiltViewModel
class ShareIntakeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sessionTokenStore: SessionTokenStore,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val sharedAnalysisRequestStore: SharedAnalysisRequestStore,
    private val crashReporter: CrashReporter,
    private val performanceTracer: PerformanceTracer,
    @ApplicationContext context: Context,
) : ViewModel() {
    private val intentParser = ShareImageIntentParser(context.contentResolver)
    private val packageName = context.packageName
    private val restoredPending = restorePendingShareIntake()
    private val _pendingShareIntake = MutableStateFlow(restoredPending)
    val pendingShareIntake: StateFlow<PendingShareIntake?> = _pendingShareIntake.asStateFlow()

    private val _isLoading = MutableStateFlow(restoredPending == null)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val eventChannel = Channel<ShareIntakeEvent>(capacity = Channel.BUFFERED)
    val events: Flow<ShareIntakeEvent> = eventChannel.receiveAsFlow()

    internal var ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    internal var copyIntent: (Intent) -> Intent = ::Intent
    internal var parseIntent: (Intent) -> ShareImageParseResult? = intentParser::parse
    internal var fingerprintIntent: (Intent) -> String? = intentParser::fingerprint

    private var parseJob: Job? = null
    private var inFlightFingerprint: String? = null
    private var isSubmittingStart = false
    private var activeShareTrace: PerformanceTrace? = null

    fun submitShareIntent(
        intent: Intent,
        forceNewSession: Boolean = false,
    ) {
        val intentCopy = copyIntent(intent)
        val fingerprint = runCatching { fingerprintIntent(intentCopy) }.getOrNull()
        if (fingerprint == null) {
            _isLoading.value = false
            stopActiveShareTrace(gate = "unsupported", outcome = "reject")
            return
        }
        val lastProcessedFingerprint = savedStateHandle.get<String>(
            LAST_PROCESSED_SHARE_FINGERPRINT_KEY,
        )
        if (!forceNewSession && fingerprint == inFlightFingerprint) {
            return
        }
        if (!forceNewSession && fingerprint == lastProcessedFingerprint) {
            _isLoading.value = false
            return
        }

        parseJob?.cancel()
        stopActiveShareTrace(gate = "superseded", outcome = "cancel")
        inFlightFingerprint = fingerprint
        _isLoading.value = true
        val trace = performanceTracer.startTrace(PerformanceTraceNames.SHARE_INTAKE_TO_ORGANIZE)
        activeShareTrace = trace
        crashReporter.setCustomKey(ObservabilityKeys.SHARE_ENTRY, "external")
        parseJob = viewModelScope.launch {
            val result = try {
                withContext(ioDispatcher) {
                    parseIntent(intentCopy)
                }
            } catch (cancellation: CancellationException) {
                stopShareTrace(
                    trace = trace,
                    gate = "cancelled",
                    outcome = "cancel",
                )
                throw cancellation
            } catch (error: RuntimeException) {
                crashReporter.recordException(error)
                ShareImageParseResult(accepted = emptyList(), rejectedCount = 0)
            }
            var keepLoadingForRedirect = false
            if (result != null) {
                savedStateHandle[LAST_PROCESSED_SHARE_FINGERPRINT_KEY] = fingerprint
                if (OnboardingSampleShareDetector.isOnboardingSampleShare(
                        images = result.accepted,
                        packageName = packageName,
                    )
                ) {
                    updatePendingShareIntake(null)
                    eventChannel.send(ShareIntakeEvent.ReturnAfterOnboardingSampleShare)
                    keepLoadingForRedirect = true
                    crashReporter.setCustomKey(ObservabilityKeys.SHARE_ENTRY, "onboarding_sample")
                    stopShareTrace(
                        trace = trace,
                        gate = "onboarding_sample",
                        outcome = "redirect",
                    )
                } else {
                    when (val gate = resolveShareEntryGate()) {
                        ShareEntryGate.LoginRequired -> {
                            updatePendingShareIntake(null)
                            eventChannel.send(ShareIntakeEvent.LoginRequired)
                            keepLoadingForRedirect = true
                            stopShareTrace(
                                trace = trace,
                                gate = "login_required",
                                outcome = "gate",
                            )
                        }

                        ShareEntryGate.OnboardingRequired -> {
                            updatePendingShareIntake(null)
                            eventChannel.send(ShareIntakeEvent.OnboardingRequired)
                            keepLoadingForRedirect = true
                            stopShareTrace(
                                trace = trace,
                                gate = "onboarding_required",
                                outcome = "gate",
                            )
                        }

                        ShareEntryGate.Allowed -> {
                            if (result.accepted.isEmpty()) {
                                updatePendingShareIntake(null)
                                stopShareTrace(
                                    trace = trace,
                                    gate = "empty",
                                    outcome = "reject",
                                )
                            } else {
                                updatePendingShareIntake(
                                    result.toPendingShareIntake(
                                        sessionId = UUID.randomUUID().toString(),
                                    ),
                                )
                                // Trace continues until requestStartOrganize or discard.
                            }
                        }
                    }
                }
            } else {
                stopShareTrace(
                    trace = trace,
                    gate = "parse_null",
                    outcome = "reject",
                )
            }
            if (inFlightFingerprint == fingerprint) {
                inFlightFingerprint = null
            }
            if (!keepLoadingForRedirect) {
                _isLoading.value = false
            }
        }
    }

    fun completePendingShareIntake(sessionId: String) {
        if (_pendingShareIntake.value?.sessionId == sessionId) {
            updatePendingShareIntake(null)
            stopActiveShareTrace(gate = "discard", outcome = "cancel")
        }
    }

    fun discardPendingShareIntake() {
        updatePendingShareIntake(null)
        stopActiveShareTrace(gate = "discard", outcome = "cancel")
    }

    fun requestStartOrganize(candidates: List<ScreenshotUploadCandidate>) {
        if (isSubmittingStart || candidates.isEmpty()) {
            return
        }
        isSubmittingStart = true
        val trace = activeShareTrace
        viewModelScope.launch {
            when (resolveShareEntryGate()) {
                ShareEntryGate.LoginRequired -> {
                    updatePendingShareIntake(null)
                    isSubmittingStart = false
                    eventChannel.send(ShareIntakeEvent.LoginRequired)
                    stopShareTrace(
                        trace = trace,
                        gate = "login_required",
                        outcome = "gate",
                    )
                    return@launch
                }

                ShareEntryGate.OnboardingRequired -> {
                    updatePendingShareIntake(null)
                    isSubmittingStart = false
                    eventChannel.send(ShareIntakeEvent.OnboardingRequired)
                    stopShareTrace(
                        trace = trace,
                        gate = "onboarding_required",
                        outcome = "gate",
                    )
                    return@launch
                }

                ShareEntryGate.Allowed -> Unit
            }
            val requestId = UUID.randomUUID().toString()
            val images = candidates.map { candidate -> candidate.localImage }
            sharedAnalysisRequestStore.register(
                requestId = requestId,
                candidates = candidates,
            )
            try {
                eventChannel.send(
                    ShareIntakeEvent.LaunchMainAnalysis(
                        requestId = requestId,
                        images = images,
                    ),
                )
                stopShareTrace(
                    trace = trace,
                    gate = "allowed",
                    outcome = "started",
                )
            } catch (cancellation: CancellationException) {
                sharedAnalysisRequestStore.consume(requestId)
                isSubmittingStart = false
                stopShareTrace(
                    trace = trace,
                    gate = "cancelled",
                    outcome = "cancel",
                )
                throw cancellation
            }
        }
    }

    override fun onCleared() {
        stopActiveShareTrace(gate = "view_model_cleared", outcome = "cancel")
        super.onCleared()
    }

    private fun stopActiveShareTrace(
        gate: String,
        outcome: String,
    ) {
        val trace = activeShareTrace ?: return
        stopShareTrace(trace = trace, gate = gate, outcome = outcome)
    }

    private fun stopShareTrace(
        trace: PerformanceTrace?,
        gate: String,
        outcome: String,
    ) {
        if (trace == null || activeShareTrace !== trace) return
        activeShareTrace = null
        trace.putAttribute(ObservabilityKeys.GATE, gate)
        trace.putAttribute(ObservabilityKeys.OUTCOME, outcome)
        trace.stop()
    }

    private suspend fun resolveShareEntryGate(): ShareEntryGate {
        val refreshToken = try {
            sessionTokenStore.getRefreshToken()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            crashReporter.recordException(error)
            null
        }
        if (refreshToken.isNullOrBlank()) {
            userPreferencesRepository.setOnboardingCompleted(false)
            return ShareEntryGate.LoginRequired
        }
        val onboardingCompleted = try {
            userPreferencesRepository.onboardingCompleted.first()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            crashReporter.recordException(error)
            false
        }
        return if (onboardingCompleted) {
            ShareEntryGate.Allowed
        } else {
            ShareEntryGate.OnboardingRequired
        }
    }

    private fun restorePendingShareIntake(): PendingShareIntake? {
        val encoded = savedStateHandle.get<String>(SHARE_INTAKE_SAVED_STATE_KEY) ?: return null
        return decodePendingShareIntakeFromSavedState(encoded)
    }

    private fun updatePendingShareIntake(pending: PendingShareIntake?) {
        _pendingShareIntake.value = pending
        if (pending == null) {
            savedStateHandle.remove<String>(SHARE_INTAKE_SAVED_STATE_KEY)
        } else {
            savedStateHandle[SHARE_INTAKE_SAVED_STATE_KEY] = pending.encodeForSavedState()
        }
    }
}

private enum class ShareEntryGate {
    Allowed,
    LoginRequired,
    OnboardingRequired,
}

private const val LAST_PROCESSED_SHARE_FINGERPRINT_KEY = "last_processed_share_fingerprint"

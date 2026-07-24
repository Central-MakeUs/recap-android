package com.chalkak.recap.app.share

import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.model.LocalImage
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ShareIntakeEvent {
    data object LoginRequired : ShareIntakeEvent

    data class LaunchMainAnalysis(
        val requestId: String,
        val images: List<LocalImage>,
    ) : ShareIntakeEvent
}

@HiltViewModel
class ShareIntakeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sessionTokenStore: SessionTokenStore,
    private val sharedAnalysisRequestStore: SharedAnalysisRequestStore,
    @ApplicationContext context: Context,
) : ViewModel() {
    private val intentParser = ShareImageIntentParser(context.contentResolver)
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

    fun submitShareIntent(
        intent: Intent,
        forceNewSession: Boolean = false,
    ) {
        val intentCopy = copyIntent(intent)
        val fingerprint = runCatching { fingerprintIntent(intentCopy) }.getOrNull()
        if (fingerprint == null) {
            _isLoading.value = false
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
        inFlightFingerprint = fingerprint
        _isLoading.value = true
        parseJob = viewModelScope.launch {
            val result = try {
                withContext(ioDispatcher) {
                    parseIntent(intentCopy)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: RuntimeException) {
                ShareImageParseResult(accepted = emptyList(), rejectedCount = 0)
            }
            if (result != null) {
                savedStateHandle[LAST_PROCESSED_SHARE_FINGERPRINT_KEY] = fingerprint
                updatePendingShareIntake(
                    result.toPendingShareIntake(sessionId = UUID.randomUUID().toString()),
                )
            }
            if (inFlightFingerprint == fingerprint) {
                inFlightFingerprint = null
            }
            _isLoading.value = false
        }
    }

    fun completePendingShareIntake(sessionId: String) {
        if (_pendingShareIntake.value?.sessionId == sessionId) {
            updatePendingShareIntake(null)
        }
    }

    fun discardPendingShareIntake() {
        updatePendingShareIntake(null)
    }

    fun requestStartOrganize(images: List<LocalImage>) {
        if (isSubmittingStart || images.isEmpty()) {
            return
        }
        isSubmittingStart = true
        viewModelScope.launch {
            val refreshToken = try {
                sessionTokenStore.getRefreshToken()
            } catch (cancellation: CancellationException) {
                isSubmittingStart = false
                throw cancellation
            } catch (_: Exception) {
                null
            }
            if (refreshToken.isNullOrBlank()) {
                isSubmittingStart = false
                eventChannel.send(ShareIntakeEvent.LoginRequired)
                return@launch
            }
            val requestId = UUID.randomUUID().toString()
            sharedAnalysisRequestStore.register(requestId = requestId, images = images)
            try {
                eventChannel.send(
                    ShareIntakeEvent.LaunchMainAnalysis(
                        requestId = requestId,
                        images = images,
                    ),
                )
            } catch (cancellation: CancellationException) {
                sharedAnalysisRequestStore.consume(requestId)
                isSubmittingStart = false
                throw cancellation
            }
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

private const val LAST_PROCESSED_SHARE_FINGERPRINT_KEY = "last_processed_share_fingerprint"

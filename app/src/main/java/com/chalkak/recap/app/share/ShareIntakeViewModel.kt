package com.chalkak.recap.app.share

import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class ShareIntakeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext context: Context,
) : ViewModel() {
    private val intentParser = ShareImageIntentParser(context.contentResolver)
    private val _pendingShareIntake = MutableStateFlow(restorePendingShareIntake())
    val pendingShareIntake: StateFlow<PendingShareIntake?> = _pendingShareIntake.asStateFlow()

    internal var ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    internal var copyIntent: (Intent) -> Intent = ::Intent
    internal var parseIntent: (Intent) -> ShareImageParseResult? = intentParser::parse
    internal var fingerprintIntent: (Intent) -> String? = intentParser::fingerprint

    private var parseJob: Job? = null
    private var inFlightFingerprint: String? = null

    fun submitShareIntent(
        intent: Intent,
        forceNewSession: Boolean = false,
    ) {
        val intentCopy = copyIntent(intent)
        val fingerprint = runCatching { fingerprintIntent(intentCopy) }.getOrNull() ?: return
        val lastProcessedFingerprint = savedStateHandle.get<String>(
            LAST_PROCESSED_SHARE_FINGERPRINT_KEY,
        )
        if (!forceNewSession &&
            (fingerprint == lastProcessedFingerprint || fingerprint == inFlightFingerprint)
        ) {
            return
        }

        parseJob?.cancel()
        inFlightFingerprint = fingerprint
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

package com.chalkak.recap.app

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.chalkak.recap.app.share.OnboardingSampleShareIntentContract
import com.chalkak.recap.app.share.OnboardingSampleShareSuccessStore
import com.chalkak.recap.app.share.SharedAnalysisIntentContract
import com.chalkak.recap.app.share.SharedAnalysisRequest
import com.chalkak.recap.app.share.SharedAnalysisRequestStore
import com.chalkak.recap.core.model.ScreenshotUploadCandidate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class MainActivityEntryViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sharedAnalysisRequestStore: SharedAnalysisRequestStore,
) : ViewModel() {
    private val _pendingHomeNavigationRequestId = MutableStateFlow(
        savedStateHandle.get<Int>(PENDING_HOME_NAVIGATION_REQUEST_ID_KEY),
    )
    val pendingHomeNavigationRequestId: StateFlow<Int?> =
        _pendingHomeNavigationRequestId.asStateFlow()

    private var homeNavigationRequestCounter =
        savedStateHandle.get<Int>(HOME_NAVIGATION_REQUEST_COUNTER_KEY) ?: 0

    fun consumeSharedAnalysisIntent(intent: Intent): List<ScreenshotUploadCandidate>? {
        val decoded = SharedAnalysisIntentContract.decode(intent) ?: return null
        return consumeSharedAnalysisRequest(decoded)
    }

    fun consumeSharedAnalysisRequest(request: SharedAnalysisRequest): List<ScreenshotUploadCandidate>? {
        val lastConsumedRequestId = savedStateHandle.get<String>(LAST_CONSUMED_REQUEST_ID_KEY)
        if (lastConsumedRequestId == request.requestId) {
            return null
        }
        // Only accept requests previously registered by this app process.
        val registered = sharedAnalysisRequestStore.consume(request.requestId) ?: return null
        savedStateHandle[LAST_CONSUMED_REQUEST_ID_KEY] = request.requestId
        requestNavigateToHome()
        return registered
    }

    fun consumeOnboardingSampleShareSuccess(intent: Intent): Boolean {
        val eventId = OnboardingSampleShareIntentContract.eventId(intent) ?: return false
        if (!OnboardingSampleShareSuccessStore.consume(eventId)) {
            return false
        }
        val lastConsumedEventId = savedStateHandle.get<String>(
            LAST_CONSUMED_ONBOARDING_SAMPLE_EVENT_ID_KEY,
        )
        if (lastConsumedEventId == eventId) {
            return false
        }
        savedStateHandle[LAST_CONSUMED_ONBOARDING_SAMPLE_EVENT_ID_KEY] = eventId
        return true
    }

    fun requestNavigateToHome() {
        val requestId = homeNavigationRequestCounter + 1
        homeNavigationRequestCounter = requestId
        savedStateHandle[HOME_NAVIGATION_REQUEST_COUNTER_KEY] = requestId
        savedStateHandle[PENDING_HOME_NAVIGATION_REQUEST_ID_KEY] = requestId
        _pendingHomeNavigationRequestId.value = requestId
    }

    fun completeHomeNavigation(requestId: Int) {
        if (_pendingHomeNavigationRequestId.value == requestId) {
            savedStateHandle.remove<Int>(PENDING_HOME_NAVIGATION_REQUEST_ID_KEY)
            _pendingHomeNavigationRequestId.value = null
        }
    }
}

private const val LAST_CONSUMED_REQUEST_ID_KEY = "last_consumed_shared_analysis_request_id"
private const val LAST_CONSUMED_ONBOARDING_SAMPLE_EVENT_ID_KEY =
    "last_consumed_onboarding_sample_event_id"
private const val PENDING_HOME_NAVIGATION_REQUEST_ID_KEY = "pending_home_navigation_request_id"
private const val HOME_NAVIGATION_REQUEST_COUNTER_KEY = "home_navigation_request_counter"

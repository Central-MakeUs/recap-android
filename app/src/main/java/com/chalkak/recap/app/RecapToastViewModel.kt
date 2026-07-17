package com.chalkak.recap.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.design.component.toast.RecapToastExitAnimationDurationMillis
import com.chalkak.recap.core.design.component.toast.RecapToastPresentation
import com.chalkak.recap.core.design.component.toast.RecapToastRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Activity-scoped toast queue owner. Survives configuration change; does not survive process death.
 */
class RecapToastViewModel : ViewModel() {
    private val requests = Channel<RecapToastRequest>(Channel.UNLIMITED)
    private val _currentToast = MutableStateFlow<RecapToastPresentation?>(null)
    val currentToast: StateFlow<RecapToastPresentation?> = _currentToast.asStateFlow()

    init {
        viewModelScope.launch {
            for (request in requests) {
                _currentToast.value = RecapToastPresentation(
                    message = request.message,
                    type = request.type,
                )
                delay(request.durationMillis.milliseconds)
                _currentToast.value = null
                delay(RecapToastExitAnimationDurationMillis.toLong().milliseconds)
            }
        }
    }

    fun enqueue(request: RecapToastRequest) {
        requests.trySend(request)
    }
}

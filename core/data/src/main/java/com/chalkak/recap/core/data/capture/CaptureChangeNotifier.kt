package com.chalkak.recap.core.data.capture

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface CaptureChange {
    data class Upserted(val captureIds: Set<Long>) : CaptureChange

    data class Deleted(val captureIds: Set<Long>) : CaptureChange

    data object Invalidated : CaptureChange
}

@Singleton
class CaptureChangeNotifier @Inject constructor() {
    private val _changes = MutableSharedFlow<CaptureChange>(extraBufferCapacity = 1)
    val changes: SharedFlow<CaptureChange> = _changes.asSharedFlow()

    suspend fun emit(change: CaptureChange) {
        _changes.emit(change)
    }
}

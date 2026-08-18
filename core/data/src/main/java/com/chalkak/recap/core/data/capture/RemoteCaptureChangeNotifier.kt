package com.chalkak.recap.core.data.capture

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteCaptureChangeNotifier @Inject constructor() {
    private val _changes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val changes: SharedFlow<Unit> = _changes.asSharedFlow()

    private val _deletedCaptureIds = MutableSharedFlow<Set<Long>>(extraBufferCapacity = 1)
    val deletedCaptureIds: SharedFlow<Set<Long>> = _deletedCaptureIds.asSharedFlow()

    fun notifyCaptureChanged() {
        _changes.tryEmit(Unit)
    }

    fun notifyCapturesDeleted(ids: Set<Long>) {
        if (ids.isNotEmpty()) {
            _deletedCaptureIds.tryEmit(ids)
        }
        notifyCaptureChanged()
    }
}

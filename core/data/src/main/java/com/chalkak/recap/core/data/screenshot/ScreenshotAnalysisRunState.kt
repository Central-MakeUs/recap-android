package com.chalkak.recap.core.data.screenshot

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenshotAnalysisRunState @Inject constructor() {
    private val activeRunCount = AtomicInteger(0)
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    fun beginRun() {
        activeRunCount.incrementAndGet()
        _isRunning.value = true
    }

    fun endRun() {
        while (true) {
            val current = activeRunCount.get()
            if (current <= 0) {
                _isRunning.value = false
                return
            }
            if (activeRunCount.compareAndSet(current, current - 1)) {
                _isRunning.value = current - 1 > 0
                return
            }
        }
    }
}

package com.chalkak.recap.app.notification

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

data class OrganizeProgressSnapshot(
    val isRunning: Boolean = false,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
)

@Singleton
class OrganizeProgressTracker @Inject constructor() {
    private val generation = AtomicInteger(0)
    private val _snapshot = MutableStateFlow(OrganizeProgressSnapshot())
    val snapshot: StateFlow<OrganizeProgressSnapshot> = _snapshot.asStateFlow()

    private val _terminalResults = MutableSharedFlow<OrganizeTerminalResult>(extraBufferCapacity = 1)
    val terminalResults: SharedFlow<OrganizeTerminalResult> = _terminalResults.asSharedFlow()

    fun onStarted(totalCount: Int): Int {
        val runId = generation.incrementAndGet()
        _snapshot.value = OrganizeProgressSnapshot(
            isRunning = true,
            completedCount = 0,
            totalCount = totalCount.coerceAtLeast(0),
        )
        return runId
    }

    fun onProgress(runId: Int, completedCount: Int, totalCount: Int) {
        if (generation.get() != runId) return
        val safeTotal = totalCount.coerceAtLeast(0)
        _snapshot.update { current ->
            if (!current.isRunning) {
                current
            } else {
                current.copy(
                    completedCount = completedCount.coerceIn(0, safeTotal.coerceAtLeast(completedCount)),
                    totalCount = safeTotal,
                )
            }
        }
    }

    fun onTerminal(runId: Int, result: OrganizeTerminalResult) {
        if (generation.get() != runId) return
        _snapshot.value = OrganizeProgressSnapshot(
            isRunning = false,
            completedCount = when (result) {
                is OrganizeTerminalResult.AllSuccess -> result.successCount
                is OrganizeTerminalResult.PartialSuccess -> result.successCount + result.failCount
                is OrganizeTerminalResult.AllFailed -> _snapshot.value.completedCount
            },
            totalCount = _snapshot.value.totalCount,
        )
        _terminalResults.tryEmit(result)
    }

    fun onCancelled(runId: Int) {
        if (generation.get() != runId) return
        _snapshot.value = OrganizeProgressSnapshot()
    }
}

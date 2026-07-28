package com.chalkak.recap.app

import com.chalkak.recap.app.notification.OrganizeTerminalResult
import com.chalkak.recap.feature.organize.OrganizeAnalysisStatusUiState

fun ScreenshotAnalysisProgressUiState.toOrganizeAnalysisStatusUiState(): OrganizeAnalysisStatusUiState {
    if (isRunning) {
        return OrganizeAnalysisStatusUiState.Progress(progress = progress)
    }
    return when (val result = terminalResult) {
        null -> OrganizeAnalysisStatusUiState.Hidden
        is OrganizeTerminalResult.AllSuccess -> OrganizeAnalysisStatusUiState.Success(
            successCount = result.successCount,
        )
        is OrganizeTerminalResult.PartialSuccess -> OrganizeAnalysisStatusUiState.PartialFailed(
            successCount = result.successCount,
        )
        OrganizeTerminalResult.AllFailed -> OrganizeAnalysisStatusUiState.Failed
    }
}

internal fun retainLastVisibleAnalysisStatus(
    previous: OrganizeAnalysisStatusUiState?,
    current: OrganizeAnalysisStatusUiState,
): OrganizeAnalysisStatusUiState? = when (current) {
    OrganizeAnalysisStatusUiState.Hidden -> previous
    else -> current
}

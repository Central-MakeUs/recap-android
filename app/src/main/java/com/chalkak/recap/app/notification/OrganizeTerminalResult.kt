package com.chalkak.recap.app.notification

import com.chalkak.recap.core.data.screenshot.analysis.ScreenshotOrganizeOutcome
import com.chalkak.recap.core.model.capture.OrganizeStatus

sealed interface OrganizeTerminalResult {
    data class AllSuccess(
        val successCount: Int,
    ) : OrganizeTerminalResult

    data class PartialSuccess(
        val successCount: Int,
        val failCount: Int,
    ) : OrganizeTerminalResult

    data class AllFailed(
        val usageLimitExceeded: Boolean = false,
    ) : OrganizeTerminalResult
}

object OrganizeTerminalResultMapper {
    fun fromRemote(outcome: ScreenshotOrganizeOutcome.RemoteCompleted): OrganizeTerminalResult {
        return when (outcome.status) {
            OrganizeStatus.COMPLETED -> OrganizeTerminalResult.AllSuccess(
                successCount = outcome.successCount,
            )

            OrganizeStatus.PARTIAL_FAILED -> OrganizeTerminalResult.PartialSuccess(
                successCount = outcome.successCount,
                failCount = outcome.failCount,
            )

            OrganizeStatus.FAILED,
            OrganizeStatus.CANCELLED,
            OrganizeStatus.PROCESSING,
                -> OrganizeTerminalResult.AllFailed()
        }
    }

    fun fromLocalPersisted(
        persistedCount: Int,
        totalCount: Int,
        saveFailed: Boolean,
    ): OrganizeTerminalResult {
        if (!saveFailed) {
            return OrganizeTerminalResult.AllSuccess(successCount = persistedCount)
        }
        if (persistedCount > 0) {
            return OrganizeTerminalResult.PartialSuccess(
                successCount = persistedCount,
                failCount = (totalCount - persistedCount).coerceAtLeast(1),
            )
        }
        return OrganizeTerminalResult.AllFailed()
    }
}

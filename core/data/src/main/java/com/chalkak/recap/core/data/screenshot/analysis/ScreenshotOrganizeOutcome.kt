package com.chalkak.recap.core.data.screenshot.analysis

import com.chalkak.recap.core.model.capture.OrganizeStatus
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult

sealed interface ScreenshotOrganizeOutcome {
    data class LocalResults(
        val results: List<ScreenshotAnalysisResult>,
    ) : ScreenshotOrganizeOutcome

    data class RemoteCompleted(
        val successCount: Int,
        val failCount: Int,
        val status: OrganizeStatus,
    ) : ScreenshotOrganizeOutcome
}

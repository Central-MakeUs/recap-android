package com.chalkak.recap.core.data.screenshot.analysis

import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.core.model.capture.OrganizeStatus
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult

sealed interface ScreenshotOrganizeOutcome {
    data class LocalResults(
        val results: List<ScreenshotAnalysisResult>,
        val sourceImages: List<LocalImage> = emptyList(),
        val preparationFailCount: Int = 0,
        val analysisFailCount: Int = 0,
    ) : ScreenshotOrganizeOutcome

    data class RemoteCompleted(
        val successCount: Int,
        val failCount: Int,
        val status: OrganizeStatus,
    ) : ScreenshotOrganizeOutcome
}

package com.chalkak.recap.core.data.screenshot.analysis

import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult

interface ScreenshotAnalysisRepository {
    suspend fun analyze(input: ScreenshotAnalysisInput): ScreenshotAnalysisResult

    suspend fun analyze(inputs: List<ScreenshotAnalysisInput>): List<ScreenshotAnalysisResult>

    suspend fun organize(
        inputs: List<ScreenshotAnalysisInput>,
        onProgress: (completed: Int, total: Int) -> Unit = { _, _ -> },
    ): ScreenshotOrganizeOutcome
}

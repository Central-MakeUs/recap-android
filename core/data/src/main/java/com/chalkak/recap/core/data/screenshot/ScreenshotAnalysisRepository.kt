package com.chalkak.recap.core.data.screenshot

import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult

interface ScreenshotAnalysisRepository {
    suspend fun analyze(input: ScreenshotAnalysisInput): ScreenshotAnalysisResult

    suspend fun analyze(inputs: List<ScreenshotAnalysisInput>): List<ScreenshotAnalysisResult>
}

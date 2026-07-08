package com.chalkak.recap.core.data.screenshot

import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult

interface ScreenshotAnalysisRepository {
    fun analyze(input: ScreenshotAnalysisInput): ScreenshotAnalysisResult

    fun analyze(inputs: List<ScreenshotAnalysisInput>): List<ScreenshotAnalysisResult>
}

package com.chalkak.recap.core.data.screenshot

import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import javax.inject.Inject
import javax.inject.Singleton

class RemoteAnalysisNotWiredException :
    Exception("Remote screenshot analysis is not wired")

@Singleton
class RemoteScreenshotAnalysisRepository @Inject constructor() : ScreenshotAnalysisRepository {
    override suspend fun analyze(input: ScreenshotAnalysisInput): ScreenshotAnalysisResult {
        throw RemoteAnalysisNotWiredException()
    }

    override suspend fun analyze(
        inputs: List<ScreenshotAnalysisInput>,
    ): List<ScreenshotAnalysisResult> {
        throw RemoteAnalysisNotWiredException()
    }
}

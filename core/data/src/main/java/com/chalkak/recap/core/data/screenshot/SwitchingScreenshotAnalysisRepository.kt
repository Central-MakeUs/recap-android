package com.chalkak.recap.core.data.screenshot

import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SwitchingScreenshotAnalysisRepository @Inject constructor(
    private val screenshotBackendModeStore: ScreenshotBackendModeStore,
    private val mockScreenshotAnalysisRepository: MockScreenshotAnalysisRepository,
    private val remoteScreenshotAnalysisRepository: RemoteScreenshotAnalysisRepository,
) : ScreenshotAnalysisRepository {
    override suspend fun analyze(input: ScreenshotAnalysisInput): ScreenshotAnalysisResult {
        return resolveDelegate().analyze(input)
    }

    override suspend fun analyze(
        inputs: List<ScreenshotAnalysisInput>,
    ): List<ScreenshotAnalysisResult> {
        return resolveDelegate().analyze(inputs)
    }

    private suspend fun resolveDelegate(): ScreenshotAnalysisRepository {
        return when (screenshotBackendModeStore.currentMode()) {
            ScreenshotBackendMode.MOCK -> mockScreenshotAnalysisRepository
            ScreenshotBackendMode.REMOTE -> remoteScreenshotAnalysisRepository
        }
    }
}

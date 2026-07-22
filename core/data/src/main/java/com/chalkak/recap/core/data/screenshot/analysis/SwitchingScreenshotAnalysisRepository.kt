package com.chalkak.recap.core.data.screenshot.analysis

import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendModeStore

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

    override suspend fun organize(
        inputs: List<ScreenshotAnalysisInput>,
        onProgress: (completed: Int, total: Int) -> Unit,
    ): ScreenshotOrganizeOutcome {
        return resolveDelegate().organize(inputs, onProgress)
    }

    private suspend fun resolveDelegate(): ScreenshotAnalysisRepository {
        return when (screenshotBackendModeStore.currentMode()) {
            ScreenshotBackendMode.MOCK -> mockScreenshotAnalysisRepository
            ScreenshotBackendMode.REMOTE -> remoteScreenshotAnalysisRepository
        }
    }
}

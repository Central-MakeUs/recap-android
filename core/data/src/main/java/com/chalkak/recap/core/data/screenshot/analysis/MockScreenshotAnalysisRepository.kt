package com.chalkak.recap.core.data.screenshot.analysis

import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class MockScreenshotAnalysisRepository @Inject constructor(
    private val randomizer: ScreenshotMockRandomizer,
) : ScreenshotAnalysisRepository {
    override suspend fun analyze(input: ScreenshotAnalysisInput): ScreenshotAnalysisResult {
        return buildResult(input)
    }

    override suspend fun analyze(inputs: List<ScreenshotAnalysisInput>): List<ScreenshotAnalysisResult> {
        return inputs.map(::buildResult)
    }

    override suspend fun organize(
        inputs: List<ScreenshotAnalysisInput>,
        onProgress: (completed: Int, total: Int) -> Unit,
    ): ScreenshotOrganizeOutcome {
        val total = inputs.size
        if (total == 0) {
            onProgress(0, 0)
            return ScreenshotOrganizeOutcome.LocalResults(emptyList())
        }
        val results = ArrayList<ScreenshotAnalysisResult>(total)
        inputs.forEachIndexed { index, input ->
            delay(MOCK_ANALYSIS_DELAY_MILLIS.milliseconds)
            results += buildResult(input)
            onProgress(index + 1, total)
        }
        return ScreenshotOrganizeOutcome.LocalResults(results)
    }

    private fun buildResult(input: ScreenshotAnalysisInput): ScreenshotAnalysisResult {
        val captureId = randomizer.captureId()
        return ScreenshotAnalysisResult(
            captureId = captureId,
            typeCode = resolveTypeCode(randomizer.contentTypeIndex()),
            title = "스크린샷${input.fileName}",
            summary = "요약${input.fileName}",
            body = "본문${input.fileName}",
            originalImageUrl = mockOriginalImageUrl(captureId),
            isFavorite = false,
            organizedAt = randomizer.organizedAt(),
        )
    }

    private fun resolveTypeCode(index: Int): ScreenshotContentType {
        return ScreenshotContentType.entries[index.coerceIn(
            ScreenshotContentType.entries.indices,
        )]
    }

    private companion object {
        const val MOCK_ANALYSIS_DELAY_MILLIS = 500L

        fun mockOriginalImageUrl(captureId: Long): String = "mock://captures/$captureId"
    }
}

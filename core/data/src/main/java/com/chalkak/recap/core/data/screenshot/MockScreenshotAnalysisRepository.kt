package com.chalkak.recap.core.data.screenshot

import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockScreenshotAnalysisRepository @Inject constructor(
    private val randomizer: ScreenshotMockRandomizer,
) : ScreenshotAnalysisRepository {
    override fun analyze(input: ScreenshotAnalysisInput): ScreenshotAnalysisResult {
        return buildResult(input)
    }

    override fun analyze(inputs: List<ScreenshotAnalysisInput>): List<ScreenshotAnalysisResult> {
        return inputs.map(::buildResult)
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
        fun mockOriginalImageUrl(captureId: Long): String = "mock://captures/$captureId"
    }
}

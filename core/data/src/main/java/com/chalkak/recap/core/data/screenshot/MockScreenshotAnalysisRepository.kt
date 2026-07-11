package com.chalkak.recap.core.data.screenshot

import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisConfidence
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.screenshot.ScreenshotContentTypes
import com.chalkak.recap.core.model.screenshot.ScreenshotKeyField
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
        return ScreenshotAnalysisResult(
            imageId = randomizer.imageId(),
            title = "스크린샷${input.fileName}",
            summary = "요약${input.fileName}",
            contentTypes = ScreenshotContentTypes(
                primaryContentType = resolveContentType(randomizer.contentTypeIndex()),
            ),
            keyFields = buildKeyFields(),
            confidence = resolveConfidence(randomizer.unitDouble()),
            isFavorite = false,
            body = "본문${input.fileName}",
        )
    }

    private fun buildKeyFields(): List<ScreenshotKeyField> {
        return listOf(
            ScreenshotKeyField(
                label = "라벨1",
                value = "값1",
                displayPriority = 1,
                isSensitive = randomizer.unitDouble() < SensitiveProbabilityThreshold,
            ),
            ScreenshotKeyField(
                label = "라벨2",
                value = "값2",
                displayPriority = 2,
                isSensitive = randomizer.unitDouble() < SensitiveProbabilityThreshold,
            ),
            ScreenshotKeyField(
                label = "라벨3",
                value = "값3",
                displayPriority = 3,
                isSensitive = randomizer.unitDouble() < SensitiveProbabilityThreshold,
            ),
        )
    }

    private fun resolveContentType(index: Int): ScreenshotContentType {
        return ScreenshotContentType.entries[index.coerceIn(
            ScreenshotContentType.entries.indices,
        )]
    }

    private fun resolveConfidence(value: Double): ScreenshotAnalysisConfidence {
        return when {
            value < HighConfidenceUpperBound -> ScreenshotAnalysisConfidence.HIGH
            value < MediumConfidenceUpperBound -> ScreenshotAnalysisConfidence.MEDIUM
            else -> ScreenshotAnalysisConfidence.LOW
        }
    }

    private companion object {
        const val SensitiveProbabilityThreshold = 0.05
        const val HighConfidenceUpperBound = 0.6
        const val MediumConfidenceUpperBound = 0.85
    }
}

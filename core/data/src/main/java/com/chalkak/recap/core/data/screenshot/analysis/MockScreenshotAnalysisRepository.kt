package com.chalkak.recap.core.data.screenshot.analysis

import com.chalkak.recap.core.data.screenshot.image.ScreenshotUploadPreparer
import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.core.model.PreparedScreenshot
import com.chalkak.recap.core.model.ScreenshotUploadCandidate
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class MockScreenshotAnalysisRepository @Inject constructor(
    private val randomizer: ScreenshotMockRandomizer,
    private val screenshotUploadPreparer: ScreenshotUploadPreparer,
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
        val preparedInputs = ArrayList<ScreenshotAnalysisInput>(total)
        var preparationFailCount = 0
        inputs.forEach { input ->
            val prepared = prepareWithRetry(input)
            if (prepared == null) {
                preparationFailCount += 1
            } else {
                preparedInputs += input.copy(
                    jpegBytes = prepared.jpegBytes,
                    contentType = PreparedScreenshot.MIME_TYPE_JPEG,
                )
            }
        }
        val results = ArrayList<ScreenshotAnalysisResult>(preparedInputs.size)
        val sourceImages = ArrayList<LocalImage>(preparedInputs.size)
        preparedInputs.forEachIndexed { index, input ->
            delay(MOCK_ANALYSIS_DELAY_MILLIS.milliseconds)
            results += buildResult(input)
            input.localImage?.let(sourceImages::add)
            onProgress(index + 1, total)
        }
        return ScreenshotOrganizeOutcome.LocalResults(
            results = results,
            sourceImages = sourceImages,
            preparationFailCount = preparationFailCount,
        )
    }

    private suspend fun prepareWithRetry(input: ScreenshotAnalysisInput): PreparedScreenshot? {
        input.jpegBytes?.takeIf { it.isNotEmpty() }?.let { bytes ->
            val image = input.localImage ?: return null
            return PreparedScreenshot(image, bytes)
        }
        val image = input.localImage ?: return null
        val remainingAttempts = (
            ScreenshotUploadCandidate.MAX_PREPARATION_ATTEMPTS -
                input.completedPreparationAttempts
            ).coerceAtLeast(0)
        repeat(remainingAttempts) {
            try {
                return screenshotUploadPreparer.prepare(image)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                // One retry is allowed across confirmation and progress preparation.
            }
        }
        return null
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
        const val MOCK_ANALYSIS_DELAY_MILLIS = 750L

        fun mockOriginalImageUrl(captureId: Long): String = "mock://captures/$captureId"
    }
}

package com.chalkak.recap.core.data.screenshot.analysis

import com.chalkak.recap.core.data.screenshot.image.ScreenshotUploadPreparer
import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.core.model.PreparedScreenshot
import com.chalkak.recap.core.model.ScreenshotUploadCandidate
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class DemoScreenshotAnalysisException(
    val fileName: String,
) : Exception("No demo analysis result for fileName=$fileName")

@Singleton
class DemoScreenshotAnalysisRepository(
    private val screenshotUploadPreparer: ScreenshotUploadPreparer,
    private val nextDelayMillis: () -> Long,
) : ScreenshotAnalysisRepository {
    @Inject
    constructor(
        screenshotUploadPreparer: ScreenshotUploadPreparer,
    ) : this(
        screenshotUploadPreparer,
        { randomAnalysisDelayMillis() },
    )

    override suspend fun analyze(input: ScreenshotAnalysisInput): ScreenshotAnalysisResult {
        val result = catalogResultOrThrow(input.fileName)
        delay(nextDelayMillis().milliseconds)
        return result
    }

    override suspend fun analyze(inputs: List<ScreenshotAnalysisInput>): List<ScreenshotAnalysisResult> {
        return inputs.map { analyze(it) }
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
        delay(INITIAL_ANALYSIS_DELAY_MILLIS.milliseconds)
        val results = ArrayList<ScreenshotAnalysisResult>(total)
        val sourceImages = ArrayList<LocalImage>(total)
        var preparationFailCount = 0
        var analysisFailCount = 0
        inputs.forEachIndexed { index, input ->
            val catalogResult = DemoScreenshotAnalysisCatalog.resultForFileName(input.fileName)
            if (catalogResult == null) {
                analysisFailCount += 1
                onProgress(index + 1, total)
                return@forEachIndexed
            }
            val prepared = prepareWithRetry(input)
            if (prepared == null) {
                preparationFailCount += 1
                onProgress(index + 1, total)
                return@forEachIndexed
            }
            delay(nextDelayMillis().milliseconds)
            results += catalogResult
            input.localImage?.let(sourceImages::add)
            onProgress(index + 1, total)
        }
        return ScreenshotOrganizeOutcome.LocalResults(
            results = results,
            sourceImages = sourceImages,
            preparationFailCount = preparationFailCount,
            analysisFailCount = analysisFailCount,
        )
    }

    private fun catalogResultOrThrow(fileName: String): ScreenshotAnalysisResult {
        return DemoScreenshotAnalysisCatalog.resultForFileName(fileName)
            ?: throw DemoScreenshotAnalysisException(fileName)
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

    internal companion object {
        const val INITIAL_ANALYSIS_DELAY_MILLIS = 1_500L
        const val MIN_ANALYSIS_DELAY_MILLIS = 800L
        const val MAX_ANALYSIS_DELAY_MILLIS = 1000L

        fun randomAnalysisDelayMillis(): Long =
            Random.nextLong(MIN_ANALYSIS_DELAY_MILLIS, MAX_ANALYSIS_DELAY_MILLIS + 1)
    }
}

package com.chalkak.recap.core.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import com.chalkak.recap.core.model.Confidence
import com.chalkak.recap.core.model.ConfidenceLevel
import com.chalkak.recap.core.model.RecapAnalysisResult
import com.chalkak.recap.core.model.RecapAnalysisBatchResult
import com.chalkak.recap.core.model.RecapAnalysisInputMode
import com.chalkak.recap.core.model.RecapAnalysisRequestMode
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.content
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
class RecapAnalysisRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val generativeModel: GenerativeModel,
) {
    suspend fun analyze(
        screenshots: List<RecapAnalysisScreenshotInput>,
        inputMode: RecapAnalysisInputMode,
        requestMode: RecapAnalysisRequestMode,
    ): RecapAnalysisBatchResult {
        val batchSize = requestMode.batchSize()
        val results = screenshots
            .chunked(batchSize)
            .flatMap { batch ->
                analyzeBatch(
                    screenshots = batch,
                    inputMode = inputMode,
                    requestMode = requestMode,
                    batchSize = batchSize,
                ).results
            }

        return RecapAnalysisBatchResult(
            analysisVersion = AnalysisVersion,
            inputMode = inputMode,
            requestMode = requestMode,
            batchSize = batchSize,
            results = results,
        )
    }

    private suspend fun analyzeBatch(
        screenshots: List<RecapAnalysisScreenshotInput>,
        inputMode: RecapAnalysisInputMode,
        requestMode: RecapAnalysisRequestMode,
        batchSize: Int,
    ): RecapAnalysisBatchResult = withContext(Dispatchers.IO) {
        val promptText = buildRecapAnalysisPrompt(
            inputMode = inputMode,
            requestMode = requestMode,
            batchSize = batchSize,
            imageIds = screenshots.map(RecapAnalysisScreenshotInput::imageId),
        )
        val content = content {
            text(promptText)
            screenshots.forEach { screenshot ->
                text(
                    """
                    image_id: ${screenshot.imageId}
                    source_uri: ${screenshot.uri}
                    """.trimIndent(),
                )
                if (inputMode != RecapAnalysisInputMode.OCR_TEXT_ONLY) {
                    image(context.loadScaledBitmap(screenshot.uri))
                }
                if (inputMode != RecapAnalysisInputMode.IMAGE_ONLY) {
                    text(
                        """
                        OCR text for ${screenshot.imageId}:
                        ${screenshot.ocrText.ifBlank { "(empty)" }}
                        """.trimIndent(),
                    )
                }
            }
        }

        val responseText = generativeModel.generateContent(content).text.orEmpty()
        Timber.d("Recap Gemini analysis raw response: %s", responseText)
        runCatching {
            parseRecapAnalysisBatchResult(
                rawJson = responseText,
                fallbackInputMode = inputMode,
                fallbackRequestMode = requestMode,
                fallbackBatchSize = batchSize,
            )
        }.onSuccess { result ->
            Timber.d("Recap Gemini analysis parsed result: %s", result)
        }.onFailure { throwable ->
            Timber.e(throwable, "Failed to parse Recap Gemini analysis response")
        }.getOrElse {
            screenshots.toParseFallbackResult(
                inputMode = inputMode,
                requestMode = requestMode,
                batchSize = batchSize,
            )
        }
    }
}

data class RecapAnalysisScreenshotInput(
    val imageId: String,
    val uri: Uri,
    val ocrText: String,
)

private fun RecapAnalysisRequestMode.batchSize(): Int {
    return when (this) {
        RecapAnalysisRequestMode.SINGLE_PER_REQUEST -> 1
        RecapAnalysisRequestMode.BATCH_PER_REQUEST -> DefaultBatchSize
    }
}

private fun List<RecapAnalysisScreenshotInput>.toParseFallbackResult(
    inputMode: RecapAnalysisInputMode,
    requestMode: RecapAnalysisRequestMode,
    batchSize: Int,
): RecapAnalysisBatchResult {
    return RecapAnalysisBatchResult(
        analysisVersion = AnalysisVersion,
        inputMode = inputMode,
        requestMode = requestMode,
        batchSize = batchSize,
        results = map { screenshot ->
            RecapAnalysisResult(
                imageId = screenshot.imageId,
                title = "분석 확인 필요",
                summary = "Gemini 응답을 앱에서 해석하지 못했습니다. 원본 스크린샷과 OCR 텍스트를 확인해주세요.",
                contentTypes = listOf("unknown"),
                keyFields = emptyList(),
                utilityTags = listOf("needs_user_intent"),
                suggestedViews = listOf("UNDEFINED"),
                confidence = LowConfidence,
                needsReview = true,
                reviewReasons = listOf("MODEL_ERROR"),
                keywords = emptyList(),
            )
        },
    )
}

private val LowConfidence = Confidence(
    overall = ConfidenceLevel.LOW,
    title = ConfidenceLevel.LOW,
    summary = ConfidenceLevel.LOW,
    contentType = ConfidenceLevel.LOW,
    keyFields = ConfidenceLevel.LOW,
    suggestedViews = ConfidenceLevel.LOW,
)

private fun Context.loadScaledBitmap(uri: Uri): Bitmap {
    val source = ImageDecoder.createSource(contentResolver, uri)
    return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        val width = info.size.width
        val height = info.size.height
        val longestSide = max(width, height)
        if (longestSide > MaxImageSidePx) {
            val scale = MaxImageSidePx.toFloat() / longestSide.toFloat()
            decoder.setTargetSize(
                (width * scale).roundToInt().coerceAtLeast(1),
                (height * scale).roundToInt().coerceAtLeast(1),
            )
        }
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
    }
}

private const val AnalysisVersion = "v0.2"
private const val DefaultBatchSize = 5
private const val MaxImageSidePx = 1280

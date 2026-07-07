package com.chalkak.recap.core.data.ai

import com.chalkak.recap.core.model.Confidence
import com.chalkak.recap.core.model.ConfidenceLevel
import com.chalkak.recap.core.model.KeyField
import com.chalkak.recap.core.model.RecapAnalysisBatchResult
import com.chalkak.recap.core.model.RecapAnalysisInputMode
import com.chalkak.recap.core.model.RecapAnalysisRequestMode
import com.chalkak.recap.core.model.RecapAnalysisResult
import org.json.JSONArray
import org.json.JSONObject

internal fun parseRecapAnalysisBatchResult(
    rawJson: String,
    fallbackInputMode: RecapAnalysisInputMode,
    fallbackRequestMode: RecapAnalysisRequestMode,
    fallbackBatchSize: Int,
): RecapAnalysisBatchResult {
    val root = JSONObject(rawJson.stripJsonFence())
    val results = root.optJSONArray("results").orEmpty().mapObjects { result ->
        RecapAnalysisResult(
            imageId = result.optString("image_id"),
            title = result.optString("title"),
            summary = result.optString("summary"),
            contentTypes = result.optJSONArray("content_types").orEmpty().mapStrings(),
            keyFields = result.optJSONArray("key_fields").orEmpty().mapObjects { field ->
                KeyField(
                    key = field.optString("key"),
                    label = field.optString("label"),
                    value = field.optString("value"),
                    valueType = field.optString("value_type", "unknown"),
                    displayPriority = field.optInt("display_priority"),
                )
            }.sortedBy(KeyField::displayPriority),
            utilityTags = result.optJSONArray("utility_tags").orEmpty().mapStrings(),
            suggestedViews = result.optJSONArray("suggested_views").orEmpty().mapStrings(),
            confidence = result.optJSONObject("confidence").toConfidence(),
            needsReview = result.optBoolean("needs_review"),
            reviewReasons = result.optJSONArray("review_reasons").orEmpty().mapStrings(),
            keywords = result.optJSONArray("keywords").orEmpty().mapStrings(),
        ).normalizePolicy()
    }

    return RecapAnalysisBatchResult(
        analysisVersion = root.optString("analysis_version", "v0.2"),
        inputMode = root.optString("input_mode").toEnumOrDefault(fallbackInputMode),
        requestMode = root.optString("request_mode").toEnumOrDefault(fallbackRequestMode),
        batchSize = root.optInt("batch_size", fallbackBatchSize),
        results = results,
    )
}

private fun RecapAnalysisResult.normalizePolicy(): RecapAnalysisResult {
    val normalizedSuggestedViews = if (suggestedViews.contains(UndefinedView)) {
        listOf(UndefinedView)
    } else {
        suggestedViews
    }
    val hasSensitiveSignal = utilityTags.contains(SensitiveTag) ||
            reviewReasons.contains(SensitiveReviewReason)
    val normalizedUtilityTags = if (hasSensitiveSignal && SensitiveTag !in utilityTags) {
        utilityTags + SensitiveTag
    } else {
        utilityTags
    }
    val normalizedReviewReasons = if (hasSensitiveSignal && SensitiveReviewReason !in reviewReasons) {
        reviewReasons + SensitiveReviewReason
    } else {
        reviewReasons
    }
    val normalizedConfidence = if (hasSensitiveSignal) {
        confidence.copy(overall = ConfidenceLevel.LOW)
    } else {
        confidence
    }

    return copy(
        suggestedViews = normalizedSuggestedViews,
        utilityTags = normalizedUtilityTags,
        confidence = normalizedConfidence,
        needsReview = needsReview || normalizedSuggestedViews.contains(UndefinedView) || hasSensitiveSignal,
        reviewReasons = normalizedReviewReasons,
    )
}

private fun JSONObject?.toConfidence(): Confidence {
    return Confidence(
        overall = this?.optString("overall").toConfidenceLevel(),
        title = this?.optString("title").toConfidenceLevel(),
        summary = this?.optString("summary").toConfidenceLevel(),
        contentType = this?.optString("content_type").toConfidenceLevel(),
        keyFields = this?.optString("key_fields").toConfidenceLevel(),
        suggestedViews = this?.optString("suggested_views").toConfidenceLevel(),
    )
}

private fun String?.toConfidenceLevel(): ConfidenceLevel {
    return toEnumOrDefault(ConfidenceLevel.LOW)
}

private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(defaultValue: T): T {
    return runCatching {
        enumValueOf<T>(orEmpty())
    }.getOrDefault(defaultValue)
}

private fun JSONArray?.orEmpty(): JSONArray = this ?: JSONArray()

private fun JSONArray.mapStrings(): List<String> {
    return List(length()) { index ->
        optString(index)
    }.filter(String::isNotBlank)
}

private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> {
    return buildList {
        for (index in 0 until length()) {
            optJSONObject(index)?.let { item ->
                add(transform(item))
            }
        }
    }
}

private fun String.stripJsonFence(): String {
    val trimmed = trim()
    if (!trimmed.startsWith("```")) {
        return trimmed
    }
    val lines = trimmed.lines().drop(1)
    return lines
        .dropLastWhile { it.trim() == "```" }
        .joinToString(separator = "\n")
        .trim()
}

private const val UndefinedView = "UNDEFINED"
private const val SensitiveTag = "sensitive"
private const val SensitiveReviewReason = "SENSITIVE_INFO_DETECTED"

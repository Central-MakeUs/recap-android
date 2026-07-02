package com.chalkak.recap.core.model

enum class RecapAnalysisInputMode {
    IMAGE_ONLY,
    OCR_TEXT_ONLY,
    IMAGE_WITH_OCR_TEXT,
}

enum class RecapAnalysisRequestMode {
    SINGLE_PER_REQUEST,
    BATCH_PER_REQUEST,
}

enum class ConfidenceLevel {
    HIGH,
    MEDIUM,
    LOW,
}

data class RecapAnalysisBatchResult(
    val analysisVersion: String,
    val inputMode: RecapAnalysisInputMode,
    val requestMode: RecapAnalysisRequestMode,
    val batchSize: Int,
    val results: List<RecapAnalysisResult>,
)

data class RecapAnalysisResult(
    val imageId: String,
    val title: String,
    val summary: String,
    val contentTypes: List<String>,
    val keyFields: List<KeyField>,
    val utilityTags: List<String>,
    val suggestedViews: List<String>,
    val confidence: Confidence,
    val needsReview: Boolean,
    val reviewReasons: List<String>,
    val keywords: List<String>,
)

data class KeyField(
    val key: String,
    val label: String,
    val value: String,
    val valueType: String,
    val displayPriority: Int,
)

data class Confidence(
    val overall: ConfidenceLevel,
    val title: ConfidenceLevel,
    val summary: ConfidenceLevel,
    val contentType: ConfidenceLevel,
    val keyFields: ConfidenceLevel,
    val suggestedViews: ConfidenceLevel,
)

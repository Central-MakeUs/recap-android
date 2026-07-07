package com.chalkak.recap.core.data.ai

import com.chalkak.recap.core.model.ConfidenceLevel
import com.chalkak.recap.core.model.RecapAnalysisInputMode
import com.chalkak.recap.core.model.RecapAnalysisRequestMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecapAnalysisJsonParserTest {
    @Test
    fun parseRecapAnalysisBatchResult_parsesFencedJsonAndSortsKeyFields() {
        val result = parseRecapAnalysisBatchResult(
            rawJson = """
                ```json
                {
                  "analysis_version": "v0.2",
                  "input_mode": "IMAGE_WITH_OCR_TEXT",
                  "request_mode": "BATCH_PER_REQUEST",
                  "batch_size": 5,
                  "results": [
                    {
                      "image_id": "screenshot_1",
                      "title": "예약 확인",
                      "summary": "호텔 예약 번호와 체크인 일정이 포함된 화면입니다.",
                      "content_types": ["reservation", "travel"],
                      "key_fields": [
                        {
                          "key": "reservation_number",
                          "label": "예약번호",
                          "value": "ABC123",
                          "value_type": "text",
                          "display_priority": 2
                        },
                        {
                          "key": "check_in",
                          "label": "체크인",
                          "value": "2026-07-03",
                          "value_type": "datetime",
                          "display_priority": 1
                        }
                      ],
                      "utility_tags": ["has_schedule"],
                      "suggested_views": ["ACTION"],
                      "confidence": {
                        "overall": "HIGH",
                        "title": "HIGH",
                        "summary": "MEDIUM",
                        "content_type": "HIGH",
                        "key_fields": "MEDIUM",
                        "suggested_views": "HIGH"
                      },
                      "needs_review": false,
                      "review_reasons": [],
                      "keywords": ["예약", "호텔"]
                    }
                  ]
                }
                ```
            """.trimIndent(),
            fallbackInputMode = RecapAnalysisInputMode.IMAGE_ONLY,
            fallbackRequestMode = RecapAnalysisRequestMode.SINGLE_PER_REQUEST,
            fallbackBatchSize = 1,
        )

        assertEquals("v0.2", result.analysisVersion)
        assertEquals(RecapAnalysisInputMode.IMAGE_WITH_OCR_TEXT, result.inputMode)
        assertEquals(RecapAnalysisRequestMode.BATCH_PER_REQUEST, result.requestMode)
        assertEquals(5, result.batchSize)
        assertEquals(1, result.results.size)

        val item = result.results.first()
        assertEquals("screenshot_1", item.imageId)
        assertEquals(listOf("reservation", "travel"), item.contentTypes)
        assertEquals(listOf("check_in", "reservation_number"), item.keyFields.map { it.key })
        assertEquals(ConfidenceLevel.HIGH, item.confidence.overall)
        assertEquals(ConfidenceLevel.MEDIUM, item.confidence.summary)
        assertEquals(listOf("예약", "호텔"), item.keywords)
    }

    @Test
    fun parseRecapAnalysisBatchResult_usesFallbacksForUnknownEnumsAndMissingConfidence() {
        val result = parseRecapAnalysisBatchResult(
            rawJson = """
                {
                  "analysis_version": "v0.2",
                  "input_mode": "BROKEN_INPUT",
                  "request_mode": "BROKEN_REQUEST",
                  "batch_size": 5,
                  "results": [
                    {
                      "image_id": "screenshot_2",
                      "title": "확인 필요",
                      "summary": "판단하기 어려운 화면입니다.",
                      "content_types": ["unknown"],
                      "key_fields": [],
                      "utility_tags": ["needs_user_intent"],
                      "suggested_views": ["UNDEFINED"],
                      "confidence": {},
                      "needs_review": true,
                      "review_reasons": ["LOW_CONFIDENCE"],
                      "keywords": []
                    }
                  ]
                }
            """.trimIndent(),
            fallbackInputMode = RecapAnalysisInputMode.OCR_TEXT_ONLY,
            fallbackRequestMode = RecapAnalysisRequestMode.SINGLE_PER_REQUEST,
            fallbackBatchSize = 1,
        )

        assertEquals(RecapAnalysisInputMode.OCR_TEXT_ONLY, result.inputMode)
        assertEquals(RecapAnalysisRequestMode.SINGLE_PER_REQUEST, result.requestMode)

        val item = result.results.first()
        assertEquals(listOf("UNDEFINED"), item.suggestedViews)
        assertTrue(item.needsReview)
        assertEquals(ConfidenceLevel.LOW, item.confidence.overall)
        assertEquals(ConfidenceLevel.LOW, item.confidence.title)
        assertEquals(listOf("LOW_CONFIDENCE"), item.reviewReasons)
    }

    @Test
    fun parseRecapAnalysisBatchResult_normalizesUndefinedViewAndSensitivePolicy() {
        val result = parseRecapAnalysisBatchResult(
            rawJson = """
                {
                  "analysis_version": "v0.2",
                  "input_mode": "IMAGE_ONLY",
                  "request_mode": "SINGLE_PER_REQUEST",
                  "batch_size": 1,
                  "results": [
                    {
                      "image_id": "screenshot_3",
                      "title": "결제 화면",
                      "summary": "민감 정보가 포함될 수 있는 결제 화면입니다.",
                      "content_types": ["payment"],
                      "key_fields": [],
                      "utility_tags": ["sensitive"],
                      "suggested_views": ["ACTION", "UNDEFINED"],
                      "confidence": {
                        "overall": "HIGH",
                        "title": "HIGH",
                        "summary": "HIGH",
                        "content_type": "HIGH",
                        "key_fields": "HIGH",
                        "suggested_views": "HIGH"
                      },
                      "needs_review": false,
                      "review_reasons": [],
                      "keywords": ["결제"]
                    }
                  ]
                }
            """.trimIndent(),
            fallbackInputMode = RecapAnalysisInputMode.IMAGE_ONLY,
            fallbackRequestMode = RecapAnalysisRequestMode.SINGLE_PER_REQUEST,
            fallbackBatchSize = 1,
        )

        val item = result.results.first()
        assertEquals(listOf("UNDEFINED"), item.suggestedViews)
        assertTrue(item.needsReview)
        assertEquals(ConfidenceLevel.LOW, item.confidence.overall)
        assertEquals(listOf("sensitive"), item.utilityTags)
        assertEquals(listOf("SENSITIVE_INFO_DETECTED"), item.reviewReasons)
    }
}

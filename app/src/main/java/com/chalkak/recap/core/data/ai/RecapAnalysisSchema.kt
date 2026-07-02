package com.chalkak.recap.core.data.ai

import com.google.firebase.ai.type.Schema

internal object RecapAnalysisSchema {
    private val confidenceLevelSchema = Schema.enumeration(
        listOf("HIGH", "MEDIUM", "LOW"),
    )

    private val confidenceSchema = Schema.obj(
        mapOf(
            "overall" to confidenceLevelSchema,
            "title" to confidenceLevelSchema,
            "summary" to confidenceLevelSchema,
            "content_type" to confidenceLevelSchema,
            "key_fields" to confidenceLevelSchema,
            "suggested_views" to confidenceLevelSchema,
        ),
    )

    private val keyFieldSchema = Schema.obj(
        mapOf(
            "key" to Schema.string(),
            "label" to Schema.string(),
            "value" to Schema.string(),
            "value_type" to Schema.string(),
            "display_priority" to Schema.integer(),
        ),
    )

    private val resultSchema = Schema.obj(
        mapOf(
            "image_id" to Schema.string(),
            "title" to Schema.string(),
            "summary" to Schema.string(),


            "content_types" to Schema.array(Schema.string()),
            "key_fields" to Schema.array(keyFieldSchema),
            "utility_tags" to Schema.array(Schema.string()),
            "suggested_views" to Schema.array(
                Schema.enumeration(
                    listOf("ACTION", "RECORD", "REFERENCE", "COMPARISON", "UNDEFINED"),
                ),
            ),
            "confidence" to confidenceSchema,
            "needs_review" to Schema.boolean(),
            "review_reasons" to Schema.array(Schema.string()),
            "keywords" to Schema.array(Schema.string()),
        ),
    )

    val batchResultSchema: Schema = Schema.obj(
        mapOf(
            "analysis_version" to Schema.string(),
            "input_mode" to Schema.enumeration(
                listOf("IMAGE_ONLY", "OCR_TEXT_ONLY", "IMAGE_WITH_OCR_TEXT"),
            ),
            "request_mode" to Schema.enumeration(
                listOf("SINGLE_PER_REQUEST", "BATCH_PER_REQUEST"),
            ),
            "batch_size" to Schema.integer(),
            "results" to Schema.array(resultSchema),
        ),
    )
}

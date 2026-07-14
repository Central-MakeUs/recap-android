package com.chalkak.recap.core.data.ai

import com.chalkak.recap.core.model.RecapAnalysisInputMode
import com.chalkak.recap.core.model.RecapAnalysisRequestMode

internal fun buildRecapAnalysisPrompt(
    inputMode: RecapAnalysisInputMode,
    requestMode: RecapAnalysisRequestMode,
    batchSize: Int,
    imageIds: List<String>,
): String {
    return """
        You are an AI assistant for RECAP, a screenshot organization service.

        Analyze the given screenshots and return structured JSON that follows the provided response schema.

        Request metadata:
        - analysis_version: v0.2
        - input_mode: ${inputMode.name}
        - request_mode: ${requestMode.name}
        - batch_size: $batchSize
        - image_ids: ${imageIds.joinToString(prefix = "[", postfix = "]")}

        Your goal is not to decide one final user intention.
        Your goal is to extract reusable information from each screenshot.

        Rules:
        - Return only data that matches the response schema.
        - Analyze each screenshot independently.
        - Return exactly one result for each image_id.
        - Use the given image_id for each result.
        - Do not invent information that is not visible in the screenshot or OCR text.
        - Respect the selected input_mode:
          - IMAGE_ONLY: analyze only the provided image.
          - OCR_TEXT_ONLY: analyze only the provided OCR text. Do not infer visual details that are not in the text.
          - IMAGE_WITH_OCR_TEXT: use both the image and OCR text. Prefer visible evidence when OCR text seems wrong.
        - If a field is not found, do not include it in key_fields.
        - key_fields must be a dynamic array of useful fields found in the screenshot.
        - Prefer the standard key_fields registry. Use custom_* only when no standard key fits.
        - A screenshot can have multiple content_types.
        - A screenshot can have multiple utility_tags.
        - A screenshot can appear in multiple suggested_views.
        - If no suggested view is clear, return suggested_views as ["UNDEFINED"] only.
        - If suggested_views is ["UNDEFINED"], set needs_review to true.
        - Use HIGH, MEDIUM, or LOW for confidence values.
        - Use LOW confidence when sensitive personal or financial information is detected.
        - If sensitive information is detected, add "sensitive" to utility_tags and "SENSITIVE_INFO_DETECTED" to review_reasons.
        - If the result is uncertain, set needs_review to true and provide review_reasons.
        - Use Korean for title, summary, label, and keywords.
        - Keep title short.
        - Keep summary useful enough that the user can understand the screenshot without opening the original image.
        - Sort key_fields by display_priority.
        - Do not perform rule-based first classification. This PoC tests Gemini-only analysis.

        Allowed content_types:
        place, product, receipt, payment, order, reservation, schedule, notice, article, job_posting, chat, map,
        travel, accommodation, sns_post, error_screen, document, unknown

        Allowed utility_tags:
        actionable, has_deadline, has_schedule, has_location, has_price, has_contact, evidence, reference,
        comparable_candidate, sensitive, searchable, needs_user_intent

        Allowed review_reasons:
        LOW_IMAGE_QUALITY, EMPTY_OR_LOW_TEXT, AMBIGUOUS_CONTENT_TYPE, MISSING_KEY_FIELDS, LOW_CONFIDENCE,
        SENSITIVE_INFO_DETECTED, UNSUPPORTED_IMAGE, MODEL_ERROR

        Standard key_fields:
        primary_subject, source_app, platform, status, important_text, date, time, datetime, deadline, period,
        created_at, place_name, address, location, url, phone, email, contact_name, price, total_amount, discount,
        payment_method, merchant, order_number, transaction_id, product_name, brand, option, quantity, shipping_fee,
        reservation_number, check_in, check_out, departure, arrival, seat, guest_count, event_name, action_required,
        next_step, company, position, salary, document_title, author, source, topic, service_name, error_code,
        error_message, custom_*
    """.trimIndent()
}

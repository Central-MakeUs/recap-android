package com.chalkak.recap.feature.collection

import androidx.annotation.StringRes
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType

@StringRes
fun ScreenshotContentType.toLabelResId(): Int = when (this) {
    ScreenshotContentType.JOB_CAREER -> R.string.collection_content_type_job_career
    ScreenshotContentType.SHOPPING_PRODUCT -> R.string.collection_content_type_shopping_product
    ScreenshotContentType.PLACE_RESTAURANT -> R.string.collection_content_type_place_restaurant
    ScreenshotContentType.SCHEDULE_RESERVATION -> R.string.collection_content_type_schedule_reservation
    ScreenshotContentType.INFO_KNOWLEDGE -> R.string.collection_content_type_info_knowledge
    ScreenshotContentType.DESIGN_REFERENCE -> R.string.collection_content_type_design_reference
    ScreenshotContentType.BOOK_CONTENT -> R.string.collection_content_type_book_content
    ScreenshotContentType.BENEFIT_EVENT -> R.string.collection_content_type_benefit_event
    ScreenshotContentType.RECORD_CAPTURE -> R.string.collection_content_type_record_capture
    ScreenshotContentType.OTHER -> R.string.collection_content_type_other
}

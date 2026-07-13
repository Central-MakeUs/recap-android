package com.chalkak.recap.core.design.category

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
    ScreenshotContentType.BOOK_CONTENT -> R.string.collection_content_type_book_content
    ScreenshotContentType.BENEFIT_EVENT -> R.string.collection_content_type_benefit_event
    ScreenshotContentType.RECORD_CAPTURE -> R.string.collection_content_type_record_capture
    ScreenshotContentType.OTHER -> R.string.collection_content_type_other
}

fun ScreenshotContentType.toRecapCategoryType(): RecapCategoryType = when (this) {
    ScreenshotContentType.JOB_CAREER -> RecapCategoryType.JobCareer
    ScreenshotContentType.SHOPPING_PRODUCT -> RecapCategoryType.ShoppingProduct
    ScreenshotContentType.PLACE_RESTAURANT -> RecapCategoryType.PlaceRestaurant
    ScreenshotContentType.SCHEDULE_RESERVATION -> RecapCategoryType.ScheduleReservation
    ScreenshotContentType.INFO_KNOWLEDGE -> RecapCategoryType.InfoKnowledge
    ScreenshotContentType.BOOK_CONTENT -> RecapCategoryType.BookContent
    ScreenshotContentType.BENEFIT_EVENT -> RecapCategoryType.BenefitEvent
    ScreenshotContentType.RECORD_CAPTURE -> RecapCategoryType.RecordCapture
    ScreenshotContentType.OTHER -> RecapCategoryType.Other
}

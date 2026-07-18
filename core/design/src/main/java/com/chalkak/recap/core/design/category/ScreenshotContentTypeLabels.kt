package com.chalkak.recap.core.design.category

import androidx.annotation.StringRes
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType

@StringRes
fun ScreenshotContentType.toLabelResId(): Int = when (this) {
    ScreenshotContentType.JOB -> R.string.collection_content_type_job_career
    ScreenshotContentType.SHOPPING -> R.string.collection_content_type_shopping_product
    ScreenshotContentType.PLACE -> R.string.collection_content_type_place_restaurant
    ScreenshotContentType.SCHEDULE -> R.string.collection_content_type_schedule_reservation
    ScreenshotContentType.KNOWLEDGE -> R.string.collection_content_type_info_knowledge
    ScreenshotContentType.CONTENT -> R.string.collection_content_type_book_content
    ScreenshotContentType.BENEFIT -> R.string.collection_content_type_benefit_event
    ScreenshotContentType.RECORD -> R.string.collection_content_type_record_capture
    ScreenshotContentType.ETC -> R.string.collection_content_type_other
}

fun ScreenshotContentType.toRecapCategoryType(): RecapCategoryType = when (this) {
    ScreenshotContentType.JOB -> RecapCategoryType.JobCareer
    ScreenshotContentType.SHOPPING -> RecapCategoryType.ShoppingProduct
    ScreenshotContentType.PLACE -> RecapCategoryType.PlaceRestaurant
    ScreenshotContentType.SCHEDULE -> RecapCategoryType.ScheduleReservation
    ScreenshotContentType.KNOWLEDGE -> RecapCategoryType.InfoKnowledge
    ScreenshotContentType.CONTENT -> RecapCategoryType.BookContent
    ScreenshotContentType.BENEFIT -> RecapCategoryType.BenefitEvent
    ScreenshotContentType.RECORD -> RecapCategoryType.RecordCapture
    ScreenshotContentType.ETC -> RecapCategoryType.Other
}

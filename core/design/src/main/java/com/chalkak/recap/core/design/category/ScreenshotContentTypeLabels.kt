package com.chalkak.recap.core.design.category

import androidx.annotation.StringRes
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType

@StringRes
fun ScreenshotContentType.toLabelResId(): Int = toRecapCategoryType().labelResId

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

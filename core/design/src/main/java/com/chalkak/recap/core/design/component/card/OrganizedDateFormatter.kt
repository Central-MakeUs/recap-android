package com.chalkak.recap.core.design.component.card

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SameYearOrganizedDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)

private val PreviousYearOrganizedDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREAN)

fun formatOrganizedAbsoluteDate(
    organizedAtMillis: Long,
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): String {
    val organizedDate = Instant.ofEpochMilli(organizedAtMillis).atZone(zoneId).toLocalDate()
    val currentYear = Instant.ofEpochMilli(nowMillis).atZone(zoneId).year
    val formatter = if (organizedDate.year == currentYear) {
        SameYearOrganizedDateFormatter
    } else {
        PreviousYearOrganizedDateFormatter
    }
    return organizedDate.format(formatter)
}

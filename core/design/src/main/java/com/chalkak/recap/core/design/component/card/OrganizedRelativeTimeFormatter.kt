package com.chalkak.recap.core.design.component.card

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

sealed interface OrganizedRelativeTimeLabel {
    data object JustNow : OrganizedRelativeTimeLabel
    data class MinutesAgo(val minutes: Int) : OrganizedRelativeTimeLabel
    data class HoursAgo(val hours: Int) : OrganizedRelativeTimeLabel
    data object Yesterday : OrganizedRelativeTimeLabel
    data class DaysAgo(val days: Int) : OrganizedRelativeTimeLabel
}

object OrganizedRelativeTimeFormatter {
    private val OneMinuteMillis = TimeUnit.MINUTES.toMillis(1)
    private val OneHourMillis = TimeUnit.HOURS.toMillis(1)
    private val OneDayMillis = TimeUnit.DAYS.toMillis(1)
    private const val MaxVisibleDayInclusive = 30L

    fun label(
        organizedAtMillis: Long,
        nowMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): OrganizedRelativeTimeLabel? {
        if (nowMillis < organizedAtMillis) {
            return OrganizedRelativeTimeLabel.JustNow
        }

        val elapsedMillis = nowMillis - organizedAtMillis
        if (elapsedMillis < OneMinuteMillis) {
            return OrganizedRelativeTimeLabel.JustNow
        }
        if (elapsedMillis < OneHourMillis) {
            val minutes = (elapsedMillis / OneMinuteMillis).toInt().coerceAtLeast(1)
            return OrganizedRelativeTimeLabel.MinutesAgo(minutes)
        }
        if (elapsedMillis < OneDayMillis) {
            val hours = (elapsedMillis / OneHourMillis).toInt().coerceAtLeast(1)
            return OrganizedRelativeTimeLabel.HoursAgo(hours)
        }

        val organizedDate = Instant.ofEpochMilli(organizedAtMillis).atZone(zoneId).toLocalDate()
        val nowDate = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
        val dayDiff = ChronoUnit.DAYS.between(organizedDate, nowDate)
        if (dayDiff > MaxVisibleDayInclusive) {
            return null
        }
        if (dayDiff == 1L) {
            return OrganizedRelativeTimeLabel.Yesterday
        }
        if (dayDiff >= 2L) {
            return OrganizedRelativeTimeLabel.DaysAgo(dayDiff.toInt())
        }

        // Same calendar day but already past 24 hours (e.g. DST edge) — treat as 1 day ago.
        return OrganizedRelativeTimeLabel.DaysAgo(1)
    }

    fun isVisible(
        organizedAtMillis: Long,
        nowMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Boolean = label(organizedAtMillis, nowMillis, zoneId) != null
}

package com.chalkak.recap.core.design.component.card

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class OrganizedRelativeTimeFormatterTest {
    private val zoneId = ZoneId.of("Asia/Seoul")

    @Test
    fun `returns just now when elapsed is under one minute`() {
        val now = millisAt(2026, 7, 9, 18, 0)
        val organizedAt = now - TimeUnit.SECONDS.toMillis(59)

        val label = OrganizedRelativeTimeFormatter.label(organizedAt, now, zoneId)

        assertEquals(OrganizedRelativeTimeLabel.JustNow, label)
    }

    @Test
    fun `returns minutes ago when elapsed is under one hour`() {
        val now = millisAt(2026, 7, 9, 18, 0)
        val organizedAt = now - TimeUnit.MINUTES.toMillis(15)

        val label = OrganizedRelativeTimeFormatter.label(organizedAt, now, zoneId)

        assertEquals(OrganizedRelativeTimeLabel.MinutesAgo(15), label)
    }

    @Test
    fun `returns hours ago when elapsed is under 24 hours`() {
        val now = millisAt(2026, 7, 9, 18, 0)
        val organizedAt = now - TimeUnit.HOURS.toMillis(3)

        val label = OrganizedRelativeTimeFormatter.label(organizedAt, now, zoneId)

        assertEquals(OrganizedRelativeTimeLabel.HoursAgo(3), label)
    }

    @Test
    fun `returns yesterday when previous calendar day and at least 24 hours elapsed`() {
        val now = millisAt(2026, 7, 9, 18, 0)
        val organizedAt = millisAt(2026, 7, 8, 10, 0)

        val label = OrganizedRelativeTimeFormatter.label(organizedAt, now, zoneId)

        assertEquals(OrganizedRelativeTimeLabel.Yesterday, label)
    }

    @Test
    fun `returns hours ago when previous calendar day but under 24 hours elapsed`() {
        val now = millisAt(2026, 7, 9, 10, 0)
        val organizedAt = millisAt(2026, 7, 8, 22, 0)

        val label = OrganizedRelativeTimeFormatter.label(organizedAt, now, zoneId)

        assertEquals(OrganizedRelativeTimeLabel.HoursAgo(12), label)
    }

    @Test
    fun `returns days ago when elapsed is between 2 and 30 days`() {
        val now = millisAt(2026, 7, 9, 18, 0)
        val organizedAt = millisAt(2026, 7, 2, 18, 0)

        val label = OrganizedRelativeTimeFormatter.label(organizedAt, now, zoneId)

        assertEquals(OrganizedRelativeTimeLabel.DaysAgo(7), label)
    }

    @Test
    fun `returns null when organized more than 30 days ago`() {
        val now = millisAt(2026, 7, 9, 18, 0)
        val organizedAt = millisAt(2026, 6, 8, 18, 0)

        val label = OrganizedRelativeTimeFormatter.label(organizedAt, now, zoneId)

        assertNull(label)
    }

    @Test
    fun `returns days ago for exactly 30 calendar days ago`() {
        val now = millisAt(2026, 7, 9, 18, 0)
        val organizedAt = millisAt(2026, 6, 9, 18, 0)

        val label = OrganizedRelativeTimeFormatter.label(organizedAt, now, zoneId)

        assertEquals(OrganizedRelativeTimeLabel.DaysAgo(30), label)
    }

    private fun millisAt(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
    ): Long {
        return LocalDateTime.of(year, month, day, hour, minute)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }
}

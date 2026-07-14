package com.chalkak.recap.core.design.component.card

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class OrganizedDateFormatterTest {
    private val seoul = ZoneId.of("Asia/Seoul")

    @Test
    fun `formats same year as month day`() {
        val march15_2026 = LocalDate.of(2026, 3, 15)
            .atStartOfDay(seoul)
            .toInstant()
            .toEpochMilli()
        val july13_2026 = LocalDate.of(2026, 7, 13)
            .atStartOfDay(seoul)
            .toInstant()
            .toEpochMilli()

        assertEquals(
            "3월 15일",
            formatOrganizedAbsoluteDate(
                organizedAtMillis = march15_2026,
                nowMillis = july13_2026,
                zoneId = seoul,
            ),
        )
    }

    @Test
    fun `formats previous year as dotted date`() {
        val dec31_2025 = LocalDate.of(2025, 12, 31)
            .atStartOfDay(seoul)
            .toInstant()
            .toEpochMilli()
        val july13_2026 = LocalDate.of(2026, 7, 13)
            .atStartOfDay(seoul)
            .toInstant()
            .toEpochMilli()

        assertEquals(
            "2025.12.31",
            formatOrganizedAbsoluteDate(
                organizedAtMillis = dec31_2025,
                nowMillis = july13_2026,
                zoneId = seoul,
            ),
        )
    }

    @Test
    fun `year boundary uses previous year format for last day of prior year`() {
        val lastDayOf2025 = LocalDate.of(2025, 12, 31)
            .atTime(23, 59)
            .atZone(seoul)
            .toInstant()
            .toEpochMilli()
        val firstDayOf2026 = LocalDate.of(2026, 1, 1)
            .atStartOfDay(seoul)
            .toInstant()
            .toEpochMilli()

        assertEquals(
            "2025.12.31",
            formatOrganizedAbsoluteDate(
                organizedAtMillis = lastDayOf2025,
                nowMillis = firstDayOf2026,
                zoneId = seoul,
            ),
        )
    }

    @Test
    fun `zone boundary can change calendar year relative to utc`() {
        val utc = ZoneId.of("UTC")
        val organizedAt = LocalDateTime.of(2026, 1, 1, 0, 30)
            .atZone(seoul)
            .toInstant()
            .toEpochMilli()
        val nowInUtc = LocalDate.of(2026, 7, 13)
            .atStartOfDay(utc)
            .toInstant()
            .toEpochMilli()

        assertEquals(
            "2025.12.31",
            formatOrganizedAbsoluteDate(
                organizedAtMillis = organizedAt,
                nowMillis = nowInUtc,
                zoneId = utc,
            ),
        )
        assertEquals(
            "1월 1일",
            formatOrganizedAbsoluteDate(
                organizedAtMillis = organizedAt,
                nowMillis = nowInUtc,
                zoneId = seoul,
            ),
        )
    }
}

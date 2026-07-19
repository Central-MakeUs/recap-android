package com.chalkak.recap.feature.settings

import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountProfileFormattersTest {
    @Test
    fun `formatJoinedDate uses yyyy_M_d without zero padding`() {
        val instant = Instant.parse("2026-06-12T15:30:00Z")
        assertEquals(
            "2026.6.12",
            formatJoinedDate(instant, ZoneOffset.UTC),
        )
    }
}

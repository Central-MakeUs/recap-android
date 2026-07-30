package com.chalkak.recap.feature.settings.account

import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

    @Test
    fun `formatJoinedDateFromIso parses server createdAt`() {
        assertEquals(
            "2026.6.12",
            formatJoinedDateFromIso("2026-06-12T00:00:00Z", ZoneOffset.UTC),
        )
    }

    @Test
    fun `formatJoinedDateFromIso returns null for invalid value`() {
        assertNull(formatJoinedDateFromIso("not-a-date"))
    }

    @Test
    fun `parseLoginPlatform maps kakao and apple case-insensitively`() {
        assertEquals(LoginPlatform.Kakao, parseLoginPlatform("kakao"))
        assertEquals(LoginPlatform.Kakao, parseLoginPlatform("KAKAO"))
        assertEquals(LoginPlatform.Apple, parseLoginPlatform("apple"))
        assertEquals(LoginPlatform.Apple, parseLoginPlatform("Apple"))
        assertEquals(LoginPlatform.Kakao, parseLoginPlatform("unknown"))
    }
}

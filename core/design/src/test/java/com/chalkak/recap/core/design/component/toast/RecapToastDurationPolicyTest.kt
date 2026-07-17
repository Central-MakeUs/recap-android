package com.chalkak.recap.core.design.component.toast

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RecapToastDurationPolicyTest {
    @Test
    fun `keeps base duration when recommendation is shorter or equal`() {
        assertEquals(
            2_000L,
            effectiveToastDurationMillis(
                baseDurationMillis = 2_000L,
                recommendedTimeoutMillis = 2_000L,
            ),
        )
        assertEquals(
            3_500L,
            effectiveToastDurationMillis(
                baseDurationMillis = 3_500L,
                recommendedTimeoutMillis = 1_000L,
            ),
        )
    }

    @Test
    fun `uses recommended timeout when longer than base`() {
        assertEquals(
            10_000L,
            effectiveToastDurationMillis(
                baseDurationMillis = 2_000L,
                recommendedTimeoutMillis = 10_000L,
            ),
        )
    }
}

package com.chalkak.recap.core.design.animation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RecapNavigationMotionOffsetsTest {

    @Test
    fun `previewPopFraction maps zero progress to zero`() {
        assertEquals(0f, RecapNavigationMotionOffsets.previewPopFraction(0f))
    }

    @Test
    fun `previewPopFraction ease-out moves more than linear at half progress`() {
        // Ease-out quadratic at 0.5: 1 - 0.25 = 0.75 → 0.35 * 0.75 = 0.2625
        assertEquals(0.2625f, RecapNavigationMotionOffsets.previewPopFraction(0.5f), 1e-4f)
    }

    @Test
    fun `previewPopFraction early progress moves more than late progress`() {
        val earlyDelta =
            RecapNavigationMotionOffsets.previewPopFraction(0.25f) -
                RecapNavigationMotionOffsets.previewPopFraction(0f)
        val lateDelta =
            RecapNavigationMotionOffsets.previewPopFraction(1f) -
                RecapNavigationMotionOffsets.previewPopFraction(0.75f)
        assertEquals(true, earlyDelta > lateDelta)
    }

    @Test
    fun `previewPopFraction maps full progress to predictive max fraction`() {
        assertEquals(
            RecapNavigationMotion.PredictiveMaxFraction,
            RecapNavigationMotionOffsets.previewPopFraction(1f),
            1e-4f,
        )
    }

    @Test
    fun `previewPopFraction clamps below zero`() {
        assertEquals(0f, RecapNavigationMotionOffsets.previewPopFraction(-0.5f))
    }

    @Test
    fun `previewPopFraction clamps above one`() {
        assertEquals(
            RecapNavigationMotion.PredictiveMaxFraction,
            RecapNavigationMotionOffsets.previewPopFraction(1.5f),
            1e-4f,
        )
    }
}

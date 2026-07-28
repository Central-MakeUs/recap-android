package com.chalkak.recap.core.design.animation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RecapSceneZIndexTrackerTest {

    @Test
    fun `consecutive pops keep each incoming scene below the outgoing scene`() {
        val tracker = RecapSceneZIndexTracker()

        assertEquals(
            -1f,
            tracker.targetZIndex(
                initialKey = "third",
                targetKey = "second",
                isPop = true,
                reuseExistingTarget = false,
            ),
        )
        tracker.retainOnly("second")

        assertEquals(
            -2f,
            tracker.targetZIndex(
                initialKey = "second",
                targetKey = "first",
                isPop = true,
                reuseExistingTarget = false,
            ),
        )
    }

    @Test
    fun `consecutive forwards keep each incoming scene above the outgoing scene`() {
        val tracker = RecapSceneZIndexTracker()

        assertEquals(
            1f,
            tracker.targetZIndex(
                initialKey = "first",
                targetKey = "second",
                isPop = false,
                reuseExistingTarget = false,
            ),
        )
        tracker.retainOnly("second")

        assertEquals(
            2f,
            tracker.targetZIndex(
                initialKey = "second",
                targetKey = "third",
                isPop = false,
                reuseExistingTarget = false,
            ),
        )
    }

    @Test
    fun `retargeting an existing scene preserves its z index`() {
        val tracker = RecapSceneZIndexTracker()
        tracker.targetZIndex(
            initialKey = "first",
            targetKey = "second",
            isPop = false,
            reuseExistingTarget = false,
        )

        assertEquals(
            0f,
            tracker.targetZIndex(
                initialKey = "second",
                targetKey = "first",
                isPop = true,
                reuseExistingTarget = true,
            ),
        )
    }
}

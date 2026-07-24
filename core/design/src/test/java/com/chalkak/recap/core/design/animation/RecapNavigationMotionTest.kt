package com.chalkak.recap.core.design.animation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RecapNavigationMotionTest {

    @Test
    fun `predictivePop uses the same transform family as pop`() {
        val pop = RecapNavigationMotion.pop()
        val predictive = RecapNavigationMotion.predictivePop()
        assertFalse(pop.isNoneTransform())
        assertFalse(predictive.isNoneTransform())
    }

    @Test
    fun `none is a none transform`() {
        assertTrue(RecapNavigationMotion.none().isNoneTransform())
    }

    @Test
    fun `predictive max fraction is thirty five percent`() {
        assertEquals(0.35f, RecapNavigationMotion.PredictiveMaxFraction)
    }

    private fun ContentTransform.isNoneTransform(): Boolean =
        targetContentEnter == EnterTransition.None && initialContentExit == ExitTransition.None
}

package com.chalkak.recap.core.design.animation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigationevent.NavigationEvent
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RecapNavigationMotionTest {

    @Test
    fun `predictivePop on edge gesture uses pop transform family`() {
        val pop = RecapNavigationMotion.pop()
        val left = RecapNavigationMotion.predictivePop(NavigationEvent.EDGE_LEFT)
        val right = RecapNavigationMotion.predictivePop(NavigationEvent.EDGE_RIGHT)
        assertFalse(pop.isNoneTransform())
        assertFalse(left.isNoneTransform())
        assertFalse(right.isNoneTransform())
    }

    @Test
    fun `predictivePop on EDGE_NONE is none`() {
        assertTrue(
            RecapNavigationMotion.predictivePop(NavigationEvent.EDGE_NONE).isNoneTransform(),
        )
    }

    @Test
    fun `none is a none transform`() {
        assertTrue(RecapNavigationMotion.none().isNoneTransform())
    }

    private fun ContentTransform.isNoneTransform(): Boolean =
        targetContentEnter == EnterTransition.None && initialContentExit == ExitTransition.None
}

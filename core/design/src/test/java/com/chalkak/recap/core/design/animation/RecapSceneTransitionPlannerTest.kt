package com.chalkak.recap.core.design.animation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RecapSceneTransitionPlannerTest {

    @Test
    fun `forward reverse to A then forward again keeps matching kind and z index`() {
        val planner = RecapSceneTransitionPlanner()

        val forward = planner.begin("A", "B", RecapNavigationKind.Forward)
        assertEquals(RecapNavigationKind.Forward, forward.kind)
        assertTrue(forward.targetContentZIndex > planner.zIndexOf("A")!!)

        planner.cancelTo("A")
        assertEquals(setOf("A"), planner.trackedKeys())
        assertNull(planner.zIndexOf("B"))
        assertNull(planner.activePlanOrNull())

        val forwardAgain = planner.begin("A", "B", RecapNavigationKind.Forward)
        assertEquals(RecapNavigationKind.Forward, forwardAgain.kind)
        assertTrue(forwardAgain.targetContentZIndex > planner.zIndexOf("A")!!)
        assertEquals(forwardAgain, planner.requirePlan("A", "B", RecapNavigationKind.Pop))
    }

    @Test
    fun `pop reverse to B then pop again keeps matching kind and z index`() {
        val planner = RecapSceneTransitionPlanner()
        planner.begin("A", "B", RecapNavigationKind.Forward)
        planner.onIdle("B")

        val pop = planner.begin("B", "A", RecapNavigationKind.Pop)
        assertEquals(RecapNavigationKind.Pop, pop.kind)
        assertTrue(pop.targetContentZIndex < planner.zIndexOf("B")!!)

        planner.cancelTo("B")
        assertEquals(setOf("B"), planner.trackedKeys())
        assertNull(planner.activePlanOrNull())

        val popAgain = planner.begin("B", "A", RecapNavigationKind.Pop)
        assertEquals(RecapNavigationKind.Pop, popAgain.kind)
        assertTrue(popAgain.targetContentZIndex < planner.zIndexOf("B")!!)
        assertEquals(popAgain, planner.requirePlan("B", "A", RecapNavigationKind.Forward))
    }

    @Test
    fun `consecutive forwards keep each incoming scene above the outgoing scene`() {
        val planner = RecapSceneTransitionPlanner()

        val ab = planner.begin("A", "B", RecapNavigationKind.Forward)
        planner.onIdle("B")
        val bc = planner.begin("B", "C", RecapNavigationKind.Forward)

        assertEquals(1f, ab.targetContentZIndex)
        assertEquals(2f, bc.targetContentZIndex)
        assertTrue(bc.targetContentZIndex > planner.zIndexOf("B")!!)
    }

    @Test
    fun `consecutive pops keep each incoming scene below the outgoing scene`() {
        val planner = RecapSceneTransitionPlanner()
        planner.begin("A", "B", RecapNavigationKind.Forward)
        planner.onIdle("B")
        planner.begin("B", "C", RecapNavigationKind.Forward)
        planner.onIdle("C")

        val cb = planner.begin("C", "B", RecapNavigationKind.Pop)
        assertEquals(1f, cb.targetContentZIndex)
        assertTrue(cb.targetContentZIndex < planner.zIndexOf("C")!!)
        planner.onIdle("B")

        val ba = planner.begin("B", "A", RecapNavigationKind.Pop)
        assertEquals(0f, ba.targetContentZIndex)
        assertTrue(ba.targetContentZIndex < planner.zIndexOf("B")!!)
    }

    @Test
    fun `replace clears previous root layer state`() {
        val planner = RecapSceneTransitionPlanner()
        planner.begin("A", "B", RecapNavigationKind.Forward)
        planner.onIdle("B")
        planner.begin("B", "C", RecapNavigationKind.Forward)
        planner.onIdle("C")

        val replace = planner.begin("C", "Root", RecapNavigationKind.Replace)

        assertEquals(RecapNavigationKind.Replace, replace.kind)
        assertEquals(0f, replace.targetContentZIndex)
        assertEquals(setOf("Root"), planner.trackedKeys())
        assertNull(planner.zIndexOf("C"))
    }

    @Test
    fun `retarget creates a new plan instead of copying previous target z index`() {
        val planner = RecapSceneTransitionPlanner()
        val ab = planner.begin("A", "B", RecapNavigationKind.Forward)
        val ac = planner.begin("A", "C", RecapNavigationKind.Forward)

        assertNull(planner.zIndexOf("B"))
        assertEquals(RecapNavigationKind.Forward, ac.kind)
        assertEquals(ab.targetContentZIndex, ac.targetContentZIndex)
        assertEquals("C", ac.targetKey)
        assertEquals(ac, planner.requirePlan("A", "C", RecapNavigationKind.Pop))
    }

    @Test
    fun `stale pair is not reused by requirePlan`() {
        val planner = RecapSceneTransitionPlanner()
        planner.begin("A", "B", RecapNavigationKind.Forward)

        val staleFallback = planner.requirePlan("B", "A", RecapNavigationKind.Pop)

        assertEquals(RecapNavigationKind.Pop, staleFallback.kind)
        assertEquals("B", staleFallback.initialKey)
        assertEquals("A", staleFallback.targetKey)
        assertTrue(staleFallback.targetContentZIndex < planner.zIndexOf("B")!!)
    }

    @Test
    fun `idle retains only the current scene`() {
        val planner = RecapSceneTransitionPlanner()
        planner.begin("A", "B", RecapNavigationKind.Forward)
        planner.begin("B", "C", RecapNavigationKind.Forward)

        planner.onIdle("C")

        assertEquals(setOf("C"), planner.trackedKeys())
        assertNull(planner.activePlanOrNull())
    }

    @Test
    fun `cancelled target does not remain as top layer for next transition`() {
        val planner = RecapSceneTransitionPlanner()
        val first = planner.begin("A", "B", RecapNavigationKind.Forward)
        planner.cancelTo("A")

        val second = planner.begin("A", "C", RecapNavigationKind.Forward)

        assertNull(planner.zIndexOf("B"))
        assertTrue(second.targetContentZIndex > planner.zIndexOf("A")!!)
        assertEquals(first.targetContentZIndex, second.targetContentZIndex)
    }
}
